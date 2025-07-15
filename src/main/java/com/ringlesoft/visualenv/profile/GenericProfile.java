package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.CliActionDefinition;
import com.ringlesoft.visualenv.model.CliParameterDefinition;
import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic environment variable profile.
 * Used for projects that don't match any specific framework profile.
 */
public class GenericProfile implements EnvProfile {
    // Group constants
    public static final String GROUP_GENERAL = "general";
    public static final String GROUP_DATABASE = "database";
    public static final String GROUP_SERVER = "server";
    
    private static final Map<String, EnvVariableDefinition> REGISTRY = new HashMap<>();
    
    static {
        // General common variables
        register("DEBUG", "Debug mode flag", Arrays.asList("true", "false"),
                EnvVariableDefinition.VariableType.BOOLEAN, GROUP_GENERAL, false);
        register("ENV", "Environment name", Arrays.asList("development", "production", "testing"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_GENERAL, false);
        register("LOG_LEVEL", "Logging level", Arrays.asList("debug", "info", "warning", "error"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_GENERAL, false);
        
        // Common server variables
        register("HOST", "Server host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_SERVER, false);
        register("PORT", "Server port", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_SERVER, false);
        
        // Common database variables
        register("DB_HOST", "Database host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_PORT", "Database port", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_DATABASE, false);
        register("DB_NAME", "Database name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_USER", "Database username", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_PASSWORD", "Database password", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, true);
    }
    
    @Override
    public String getProfileName() {
        return "Generic";
    }
    
    @Override
    public String getProfileDescription() {
        return "General purpose environment variables";
    }
    
    @Override
    public Map<String, EnvVariableDefinition> getDefinitions() {
        return Collections.unmodifiableMap(REGISTRY);
    }
    
    @Override
    public EnvVariableDefinition getDefinition(String name) {
        return REGISTRY.get(name);
    }
    
    @Override
    public List<EnvVariableDefinition> getDefinitionsForGroup(String group) {
        return REGISTRY.values().stream()
                .filter(def -> def.getGroup().equals(group))
                .collect(Collectors.toList());
    }
    
    @Override
    public Set<String> getAllGroups() {
        Set<String> groups = new HashSet<>();
        for (EnvVariableDefinition definition : REGISTRY.values()) {
            groups.add(definition.getGroup());
        }
        return Collections.unmodifiableSet(groups);
    }
    
    @Override
    public boolean isVariablePredefined(String name) {
        return REGISTRY.containsKey(name);
    }
    
    @Override
    public boolean supportsArtisanCommands() {
        return false;
    }
    
    @Override
    public String[] getCommonEnvFiles() {
        return new String[] {".env", ".env.local"};
    }


    @Override
    public List<EnvFileDefinition> getEnvFileDefinitions() {
        List<EnvFileDefinition> definitions = new ArrayList<>();
        
        // Primary .env file
        definitions.add(EnvFileDefinition.createPrimaryEnv());
        
        // Local overrides
        definitions.add(EnvFileDefinition.createLocalEnv());
        
        return definitions;
    }

    @Override
    public List<CliActionDefinition> getAvailableCliActions() {
        return List.of( );
    }
    
    /**
     * Get the appropriate environment variable listing command for the current OS
     */
    private String getOsSpecificEnvListCommand() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "set";
        } else {
            return "env";
        }
    }
    
    /**
     * Get the appropriate environment variable get command for the current OS
     */
    private String getOsSpecificEnvGetCommand() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "echo %{name}%";
        } else {
            return "echo $${name}";
        }
    }
    
    /**
     * Get the appropriate file view command for the current OS
     */
    private String getOsSpecificFileViewCommand() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "type \"%ENV_FILE_PATH%\"";
        } else {
            return "cat \"$ENV_FILE_PATH\"";
        }
    }
    
    /**
     * Get the appropriate file grep command for the current OS
     */
    private String getOsSpecificFileGrepCommand() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "findstr \"{search}\" \"%ENV_FILE_PATH%\"";
        } else {
            return "grep \"{search}\" \"$ENV_FILE_PATH\"";
        }
    }
    
    /**
     * Registers a predefined environment variable in the registry.
     *
     * @param name Name of the environment variable
     * @param description Description of the variable
     * @param possibleValues List of possible values (for dropdown variables)
     * @param type Variable type (string, boolean, dropdown, integer)
     * @param group Group the variable belongs to
     * @param secret Whether the variable contains sensitive data
     */
    private static void register(String name, String description, List<String> possibleValues,
                               EnvVariableDefinition.VariableType type, String group, boolean secret) {
        REGISTRY.put(name, new EnvVariableDefinition(name, description, possibleValues, type, group, secret));
    }
    
    @Override
    public boolean supportsTemplateFiles() {
        return false;
    }
}
