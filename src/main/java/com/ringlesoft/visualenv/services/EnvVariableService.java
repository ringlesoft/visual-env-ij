package com.ringlesoft.visualenv.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.model.EnvVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            result.add(new EnvVariable(entry.getKey(), entry.getValue(), "System", isSecret));
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
                // Skip comments and empty lines
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
                    result.add(new EnvVariable(name, value, file.getName(), isSecret));
                }
            }
        } catch (IOException e) {
            LOG.error("Error reading env file: " + file.getPath(), e);
        }
        
        // Store the results for this file
        fileEnvVariables.put(file.getPath(), result);
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
