package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.EnvVariableDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Laravel-specific environment variable profile.
 * Contains predefined variables used in Laravel projects.
 */
public class LaravelProfile implements EnvProfile {
    // Group constants
    public static final String GROUP_APP = "app";
    public static final String GROUP_DATABASE = "database";
    public static final String GROUP_LOGGING = "logging";
    public static final String GROUP_BROADCAST = "broadcast";
    public static final String GROUP_CACHE = "cache";
    public static final String GROUP_QUEUE = "queue";
    public static final String GROUP_SESSION = "session";
    public static final String GROUP_MAIL = "mail";
    public static final String GROUP_PUSHER = "pusher";
    public static final String GROUP_AWS = "aws";
    public static final String GROUP_VITE_PUSHER = "vite_pusher";

    private static final Map<String, EnvVariableDefinition> REGISTRY = new HashMap<>();

    static {
        // App variables
        register("APP_NAME", "Application name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APP, false);
        register("APP_ENV", "Application environment", Arrays.asList("local", "production", "testing", "staging"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_APP, false);
        register("APP_KEY", "Application encryption key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APP, true);
        register("APP_DEBUG", "Application debug mode", Arrays.asList("true", "false"),
                EnvVariableDefinition.VariableType.BOOLEAN, GROUP_APP, false);
        register("APP_URL", "Application URL", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APP, false);
        register("APP_TIMEZONE", "Application timezone", Arrays.asList(
                "UTC", "Europe/London", "America/New_York", "Asia/Tokyo", "Australia/Sydney"
        ), EnvVariableDefinition.VariableType.DROPDOWN, GROUP_APP, false);
        
        // Database variables
        register("DB_CONNECTION", "Database driver", Arrays.asList("mysql", "sqlite", "pgsql", "sqlsrv"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_DATABASE, false);
        register("DB_HOST", "Database host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_PORT", "Database port", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_DATABASE, false);
        register("DB_DATABASE", "Database name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_USERNAME", "Database username", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_PASSWORD", "Database password", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, true);
        
        // Mail variables
        register("MAIL_MAILER", "Mail driver", Arrays.asList("smtp", "sendmail", "mailgun", "ses", "log", "array"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_MAIL, false);
        register("MAIL_HOST", "Mail host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_MAIL, false);
        register("MAIL_PORT", "Mail port", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_MAIL, false);
        register("MAIL_USERNAME", "Mail username", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_MAIL, false);
        register("MAIL_PASSWORD", "Mail password", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_MAIL, true);
        register("MAIL_ENCRYPTION", "Mail encryption", Arrays.asList("tls", "ssl", ""),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_MAIL, false);
        register("MAIL_FROM_ADDRESS", "Mail from address", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_MAIL, false);
        register("MAIL_FROM_NAME", "Mail from name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_MAIL, false);
        
        // And many more variables...
        // Only included a subset for brevity, 
        // in the full implementation we would include all Laravel environment variables
    }
    
    @Override
    public String getProfileName() {
        return "Laravel";
    }
    
    @Override
    public String getProfileDescription() {
        return "Laravel framework environment variables";
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
        return true;
    }
    
    @Override
    public String[] getCommonEnvFiles() {
        return new String[] {".env", ".env.example", ".env.testing"};
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
}
