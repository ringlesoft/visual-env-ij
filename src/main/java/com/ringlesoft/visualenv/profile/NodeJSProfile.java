package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NodeJS-specific environment variable profile.
 * Contains predefined variables commonly used in NodeJS projects.
 */
public class NodeJSProfile implements EnvProfile {
    // Group constants
    public static final String GROUP_APP = "app";
    public static final String GROUP_SERVER = "server";
    public static final String GROUP_DATABASE = "database";
    public static final String GROUP_AUTH = "authentication";
    public static final String GROUP_APIS = "api";
    public static final String GROUP_LOGGING = "logging";

    private static final Map<String, EnvVariableDefinition> REGISTRY = new HashMap<>();

    static {
        // App variables
        register("NODE_ENV", "Node.js environment", Arrays.asList("development", "production", "test", "staging"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_APP, false);
        register("APP_NAME", "Application name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APP, false);
        register("PORT", "Server port number", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_SERVER, false);
        register("HOST", "Server host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_SERVER, false);
        register("BASE_URL", "Base URL for the application", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_SERVER, false);
        register("API_PREFIX", "API route prefix", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APIS, false);
        
        // Database variables
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
        register("MONGODB_URI", "MongoDB connection URI", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        
        // Authentication variables
        register("JWT_SECRET", "JSON Web Token secret key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_AUTH, true);
        register("JWT_EXPIRATION", "JWT expiration time (in seconds)", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_AUTH, false);
        register("SESSION_SECRET", "Session secret key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_AUTH, true);
        
        // API keys
        register("STRIPE_API_KEY", "Stripe API key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APIS, true);
        register("SENDGRID_API_KEY", "SendGrid API key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APIS, true);
        register("AWS_ACCESS_KEY", "AWS access key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APIS, true);
        register("AWS_SECRET_KEY", "AWS secret key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_APIS, true);
        
        // Logging
        register("LOG_LEVEL", "Logging level", 
                Arrays.asList("debug", "info", "warn", "error", "fatal"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_LOGGING, false);
        register("SENTRY_DSN", "Sentry error tracking DSN", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_LOGGING, false);
    }
    
    @Override
    public String getProfileName() {
        return "NodeJS";
    }
    
    @Override
    public String getProfileDescription() {
        return "Node.js environment variables";
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
        return new String[] {".env", ".env.local", ".env.development", ".env.production", ".env.test"};
    }


    @Override
    public List<EnvFileDefinition> getEnvFileDefinitions() {
        List<EnvFileDefinition> definitions = new ArrayList<>();
        
        // Primary .env file
        definitions.add(EnvFileDefinition.createPrimaryEnv());
        
        // Local overrides (highest priority for Node.js)
        EnvFileDefinition localEnv = EnvFileDefinition.createLocalEnv();
        definitions.add(localEnv);
        
        // Environment-specific files
        definitions.add(EnvFileDefinition.createDevelopmentEnv());
        definitions.add(EnvFileDefinition.createProductionEnv());
        
        // Testing environment
        EnvFileDefinition testEnv = new EnvFileDefinition(
                ".env.test",
                "Environment variables for testing",
                false,
                true,
                5,
                EnvFileDefinition.EnvFileType.TESTING
        );
        definitions.add(testEnv);
        
        return definitions;
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
