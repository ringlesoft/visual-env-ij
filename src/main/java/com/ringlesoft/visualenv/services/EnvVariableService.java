package com.ringlesoft.visualenv.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to detect and manage environment variables in a project.
 */
@Service(Service.Level.PROJECT)
public final class EnvVariableService {
    private static final Logger LOG = Logger.getInstance(EnvVariableService.class);
    private static final Pattern ENV_PATTERN = Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*=\\s*(.*)$");
    private static final Set<String> COMMON_SECRET_KEYS = Set.of(
            "PASSWORD", "SECRET", "KEY", "TOKEN", "PRIVATE", "AUTH"
    );

    private final Project project;
    private final Map<String, List<EnvVariable>> fileEnvVariables = new HashMap<>();
    private VirtualFile activeEnvFile;

    public EnvVariableService(Project project) {
        this.project = project;
        LOG.info("EnvVariableService initialized for project: " + project.getName());
    }

    /**
     * Get all environment variables from the system.
     * 
     * @return List of environment variables
     */
    public List<EnvVariable> getSystemEnvVariables() {
        List<EnvVariable> result = new ArrayList<>();
        
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            boolean isSecret = isLikelySecret(entry.getKey());
            EnvVariableDefinition definition = EnvVariableRegistry.getDefinition(entry.getKey());
            String group = definition != null ? definition.getGroup() : "system";
            result.add(new EnvVariable(entry.getKey(), entry.getValue(), "System", isSecret, group));
        }
        
        return result;
    }

    /**
     * Parse environment variables from a file.
     *
     * @param file The .env file to parse
     * @return List of detected environment variables
     */
    public List<EnvVariable> parseEnvFile(VirtualFile file) {
        List<EnvVariable> result = new ArrayList<>();
        
        if (file == null || !file.exists()) {
            return result;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines (for now)
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                Matcher matcher = ENV_PATTERN.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String value = matcher.group(2);
                    
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"") || 
                            value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    boolean isSecret = isLikelySecret(name);
                    EnvVariableDefinition definition = EnvVariableRegistry.getDefinition(name);
                    String group = definition != null ? definition.getGroup() : "other";
                    result.add(new EnvVariable(name, value, file.getName(), isSecret, group));
                }
            }
        } catch (IOException e) {
            LOG.error("Error reading env file: " + file.getPath(), e);
        }
        
        // Store the results for this file
        fileEnvVariables.put(file.getPath(), result);
        activeEnvFile = file;
        return result;
    }

    /**
     * Get all known environment variables from parsed files.
     *
     * @return Map of filepath to list of variables
     */
    public Map<String, List<EnvVariable>> getAllFileEnvVariables() {
        return Collections.unmodifiableMap(fileEnvVariables);
    }
    
    /**
     * Updates or creates an environment variable in the active .env file.
     *
     * @param name Name of the variable
     * @param value Value to set
     * @return true if successful
     */
    public boolean updateEnvVariable(String name, String value) {
        if (activeEnvFile == null || !activeEnvFile.exists()) {
            LOG.warn("No active .env file to update");
            return false;
        }

        try {
            String content = new String(activeEnvFile.contentsToByteArray(), StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            boolean found = false;
            
            StringBuilder newContent = new StringBuilder();
            
            // Format the value with quotes if it contains spaces
            String formattedValue = value.contains(" ") ? "\"" + value + "\"" : value;
            
            // Try to update existing variable
            for (String line : lines) {
                Matcher matcher = ENV_PATTERN.matcher(line);
                if (matcher.find() && matcher.group(1).equals(name)) {
                    newContent.append(name).append("=").append(formattedValue).append("\n");
                    found = true;
                } else {
                    newContent.append(line).append("\n");
                }
            }
            
            // Add new variable if not found
            if (!found) {
                newContent.append(name).append("=").append(formattedValue).append("\n");
            }
            
            // Write back to file
            activeEnvFile.setBinaryContent(newContent.toString().getBytes(StandardCharsets.UTF_8));
            
            // Re-parse the file to update our internal state
            parseEnvFile(activeEnvFile);
            
            return true;
        } catch (IOException e) {
            LOG.error("Error updating env variable: " + name, e);
            return false;
        }
    }
    
    /**
     * Creates a .env file from a .env.example file
     *
     * @param envExampleFile The .env.example file
     * @return true if successful, false otherwise
     */
    public boolean createEnvFromExample(VirtualFile envExampleFile) {
        try {
            String basePath = project.getBasePath();
            if (basePath == null) return false;
            
            // Parse the example file
            List<EnvVariable> exampleVariables = parseEnvFile(envExampleFile);
            if (exampleVariables.isEmpty()) return false;
            
            // Create the .env file
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(basePath);
            if (baseDir == null) return false;
            
            // Create or get the .env file
            VirtualFile envFile = baseDir.findChild(".env");
            if (envFile == null) {
                envFile = baseDir.createChildData(this, ".env");
            }
            
            // Build the content
            StringBuilder content = new StringBuilder();
            for (EnvVariable variable : exampleVariables) {
                content.append(variable.getName()).append('=');
                
                // Keep the example value for non-sensitive data
                if (variable.isSecret()) {
                    // For secret variables, generate a random value or leave it empty
                    if (variable.getName().contains("KEY") || variable.getName().contains("SECRET")) {
                        content.append(generateRandomKey());
                    } else {
                        content.append("");
                    }
                } else {
                    content.append(variable.getValue());
                }
                
                content.append('\n');
            }
            
            // Write to file
            try (OutputStream outputStream = envFile.getOutputStream(this)) {
                outputStream.write(content.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            // Set as active file
            activeEnvFile = envFile;
            
            return true;
        } catch (Exception e) {
            LOG.error("Error creating .env file from example", e);
            return false;
        }
    }
    
    /**
     * Generates a random string suitable for a key
     * 
     * @return A random string of 32 characters
     */
    private String generateRandomKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(32);
        Random random = new Random();
        
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    /**
     * Executes an Artisan command in the Laravel project
     *
     * @param command The command to execute (without the "php artisan" prefix)
     * @return The command output
     */
    public String executeArtisanCommand(String command) {
        try {
            String basePath = project.getBasePath();
            if (basePath == null) return "Project base path not found";
            
            // Check if artisan file exists
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(basePath);
            if (baseDir == null) return "Project base directory not found";
            
            VirtualFile artisanFile = baseDir.findChild("artisan");
            if (artisanFile == null || artisanFile.isDirectory()) {
                return "Artisan file not found. This is not a Laravel project.";
            }
            
            // Execute command
            String fullCommand = "php " + artisanFile.getPath() + " " + command;
            
            // Create process
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", fullCommand);
            processBuilder.directory(new File(basePath));
            
            // Set environment variables from active .env file
            if (activeEnvFile != null) {
                Map<String, String> environment = processBuilder.environment();
                List<EnvVariable> variables = parseEnvFile(activeEnvFile);
                
                for (EnvVariable var : variables) {
                    environment.put(var.getName(), var.getValue());
                }
            }
            
            // Execute
            Process process = processBuilder.start();
            
            // Capture output
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // Capture errors
            StringBuilder error = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
            
            // Wait for process to finish
            int exitCode = process.waitFor();
            
            // Return output
            if (exitCode == 0) {
                return output.toString();
            } else {
                return "Error (exit code " + exitCode + "):\n" + error.toString();
            }
            
        } catch (Exception e) {
            LOG.error("Error executing Artisan command", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get the currently active .env file.
     *
     * @return The active file or null if none
     */
    public VirtualFile getActiveEnvFile() {
        return activeEnvFile;
    }
    
    /**
     * Determine if a variable is likely to be a secret based on its name.
     *
     * @param name Variable name
     * @return true if likely a secret
     */
    private boolean isLikelySecret(String name) {
        String upperName = name.toUpperCase();
        for (String secretKey : COMMON_SECRET_KEYS) {
            if (upperName.contains(secretKey)) {
                return true;
            }
        }
        return false;
    }
}
