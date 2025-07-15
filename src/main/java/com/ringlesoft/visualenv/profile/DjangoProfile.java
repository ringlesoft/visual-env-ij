package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Django-specific environment variable profile.
 * Contains predefined variables commonly used in Django projects.
 */
public class DjangoProfile implements EnvProfile {
    // Group constants
    public static final String GROUP_CORE = "core";
    public static final String GROUP_DATABASE = "database";
    public static final String GROUP_EMAIL = "email";
    public static final String GROUP_AWS = "aws";
    public static final String GROUP_CACHE = "cache";
    public static final String GROUP_DEBUG = "debug";
    public static final String GROUP_SECURITY = "security";
    public static final String GROUP_API = "api";
    public static final String GROUP_STORAGE = "storage";

    private static final Map<String, EnvVariableDefinition> REGISTRY = new HashMap<>();

    static {
        // Core Django variables
        register("DJANGO_SETTINGS_MODULE", "Django settings module path", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_CORE, false);
        register("SECRET_KEY", "Secret key used for cryptographic signing", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_SECURITY, true);
        register("DEBUG", "Enable/disable debug mode", Arrays.asList("True", "False"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_DEBUG, false);
        register("ALLOWED_HOSTS", "List of allowed hosts", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_SECURITY, false);
        register("DJANGO_ENV", "Environment (development/production/etc)", 
                Arrays.asList("development", "production", "staging", "testing"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_CORE, false);
        
        // Database settings
        register("DATABASE_URL", "Database connection URL", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_ENGINE", "Database engine", 
                Arrays.asList("django.db.backends.postgresql", "django.db.backends.mysql", 
                        "django.db.backends.sqlite3", "django.db.backends.oracle"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_DATABASE, false);
        register("DB_NAME", "Database name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_USER", "Database username", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_PASSWORD", "Database password", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, true);
        register("DB_HOST", "Database host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_DATABASE, false);
        register("DB_PORT", "Database port", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_DATABASE, false);
        
        // Email settings
        register("EMAIL_BACKEND", "Email backend", 
                Arrays.asList(
                        "django.core.mail.backends.smtp.EmailBackend",
                        "django.core.mail.backends.console.EmailBackend",
                        "django.core.mail.backends.filebased.EmailBackend"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_EMAIL, false);
        register("EMAIL_HOST", "SMTP server host", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_EMAIL, false);
        register("EMAIL_PORT", "SMTP server port", Collections.emptyList(),
                EnvVariableDefinition.VariableType.INTEGER, GROUP_EMAIL, false);
        register("EMAIL_HOST_USER", "SMTP server username", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_EMAIL, false);
        register("EMAIL_HOST_PASSWORD", "SMTP server password", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_EMAIL, true);
        register("EMAIL_USE_TLS", "Use TLS for SMTP", Arrays.asList("True", "False"),
                EnvVariableDefinition.VariableType.DROPDOWN, GROUP_EMAIL, false);
        
        // Cache settings
        register("CACHE_URL", "Cache backend URL", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_CACHE, false);
        register("REDIS_URL", "Redis connection URL", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_CACHE, false);
        
        // AWS settings
        register("AWS_ACCESS_KEY_ID", "AWS access key ID", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_AWS, false);
        register("AWS_SECRET_ACCESS_KEY", "AWS secret access key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_AWS, true);
        register("AWS_STORAGE_BUCKET_NAME", "AWS S3 bucket name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_AWS, false);
        register("AWS_S3_REGION_NAME", "AWS S3 region name", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_AWS, false);
                
        // API settings
        register("API_KEY", "API key for third-party services", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_API, true);
        register("STRIPE_API_KEY", "Stripe payment API key", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_API, true);
        register("SENTRY_DSN", "Sentry error tracking DSN", Collections.emptyList(),
                EnvVariableDefinition.VariableType.STRING, GROUP_API, false);
    }
    
    @Override
    public String getProfileName() {
        return "Django";
    }
    
    @Override
    public String getProfileDescription() {
        return "Django environment variables";
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
        return new String[] {".env", ".env.local", ".env.development", ".env.production"};
    }

    
    @Override
    public List<EnvFileDefinition> getEnvFileDefinitions() {
        List<EnvFileDefinition> definitions = new ArrayList<>();
        
        // Primary .env file
        definitions.add(EnvFileDefinition.createPrimaryEnv());
        
        // Local overrides
        definitions.add(EnvFileDefinition.createLocalEnv());
        
        // Environment-specific files
        definitions.add(EnvFileDefinition.createDevelopmentEnv());
        definitions.add(EnvFileDefinition.createProductionEnv());
        
        // Python-style .env file
        EnvFileDefinition pythonEnv = new EnvFileDefinition(
                ".env.py",
                "Python module environment variables",
                false,
                true,
                6,
                EnvFileDefinition.EnvFileType.CUSTOM
        );
        definitions.add(pythonEnv);
        
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
