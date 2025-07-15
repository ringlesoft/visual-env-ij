package com.ringlesoft.visualenv.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.profile.ProfileManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing environment variables in the project.
 */
@Service(Service.Level.PROJECT)
public final class EnvVariableService {
    private static final Logger LOG = Logger.getInstance(EnvVariableService.class);
    private final Project project;
    private final Map<VirtualFile, List<EnvVariable>> fileEnvVariables = new HashMap<>();
    private VirtualFile activeEnvFile;
    private EnvProfile activeProfile;

    /**
     * Create a new EnvVariableService for a project
     *
     * @param project The project
     */
    public EnvVariableService(Project project) {
        this.project = project;
        // Initialize the active profile based on project type
        this.activeProfile = ProfileManager.getProfileForProject(project);
        LOG.info("EnvVariableService initialized with profile: " + activeProfile.getProfileName());
    }

    /**
     * Parse an environment file and extract variables
     *
     * @param file The file to parse
     * @return List of environment variables
     */
    public List<EnvVariable> parseEnvFile(VirtualFile file) {
        List<EnvVariable> variables = new ArrayList<>();
        
        // Store file as active
        activeEnvFile = file;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines (for now)
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                // Match key=value pattern
                Pattern pattern = Pattern.compile("^([^=]+)=(.*)$");
                Matcher matcher = pattern.matcher(line);
                
                if (matcher.matches()) {
                    String name = matcher.group(1).trim();
                    String value = matcher.group(2).trim();
                    
                    // Remove quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) || 
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    // Check if this is a predefined variable
                    EnvVariableDefinition definition = activeProfile.getDefinition(name);
                    
                    // Determine group
                    String group = (definition != null) ? definition.getGroup() : "other";
                    
                    // Determine if secret
                    boolean isSecret = (definition != null) ? definition.isSecret() : 
                            name.toLowerCase().contains("key") || 
                            name.toLowerCase().contains("secret") || 
                            name.toLowerCase().contains("password") ||
                            name.toLowerCase().contains("token");
                    
                    EnvVariable variable = new EnvVariable(name, value, file, group, isSecret);
                    variables.add(variable);
                }
            }
            
            // Cache variables
            fileEnvVariables.put(file, variables);
            
            return variables;
        } catch (IOException e) {
            LOG.error("Failed to parse env file", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Update an environment variable in the active file
     *
     * @param name  The name of the variable
     * @param value The new value
     * @return true if successful
     */
    public boolean updateEnvVariable(String name, String value) {
        if (activeEnvFile == null) {
            LOG.error("No active env file");
            return false;
        }
        
        try {
            // Read the file content
            String content = new String(activeEnvFile.contentsToByteArray(), StandardCharsets.UTF_8);
            String updatedContent;
            
            // Check if the variable exists in the file
            Pattern pattern = Pattern.compile("^" + Pattern.quote(name) + "=.*$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                // Update existing variable
                updatedContent = matcher.replaceFirst(name + "=" + value);
            } else {
                // Add new variable
                updatedContent = content + (content.endsWith("\n") ? "" : "\n") + name + "=" + value + "\n";
            }
            
            // Write the content back to the file
            try (OutputStream outputStream = activeEnvFile.getOutputStream(this)) {
                outputStream.write(updatedContent.getBytes(StandardCharsets.UTF_8));
            }
            
            // Update cache
            parseEnvFile(activeEnvFile);
            
            return true;
        } catch (IOException e) {
            LOG.error("Failed to update env variable", e);
            return false;
        }
    }
    
    /**
     * Get all environment variables from all loaded files
     *
     * @return Map of files to their variables
     */
    public Map<String, List<EnvVariable>> getAllFileEnvVariables() {
        Map<String, List<EnvVariable>> result = new HashMap<>();
        for (Map.Entry<VirtualFile, List<EnvVariable>> entry : fileEnvVariables.entrySet()) {
            result.put(entry.getKey().getPath(), entry.getValue());
        }
        return result;
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
     * @return The active .env file, or null if none is active
     */
    public VirtualFile getActiveEnvFile() {
        return activeEnvFile;
    }

    /**
     * Get the currently active profile
     * 
     * @return The active profile
     */
    public EnvProfile getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Set the active profile
     * 
     * @param profile The profile to set as active
     */
    public void setActiveProfile(EnvProfile profile) {
        this.activeProfile = profile;
        LOG.info("Active profile changed to: " + profile.getProfileName());
        
        // Re-parse active env file with new profile if there is one
        if (activeEnvFile != null) {
            parseEnvFile(activeEnvFile);
        }
    }
}
