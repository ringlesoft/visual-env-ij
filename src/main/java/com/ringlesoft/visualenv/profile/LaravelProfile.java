package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.CliActionDefinition;
import com.ringlesoft.visualenv.model.CliParameterDefinition;
import com.ringlesoft.visualenv.model.EnvFileDefinition;
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
    public static final String GROUP_REDIS = "redis";
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


        // Logging variables
        register(
                "LOG_CHANNEL",
                "Specifies the default logging channel",
                Arrays.asList("stack", "single", "daily", "slack"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_LOGGING,
                false
        );

        register(
                "LOG_LEVEL",
                "Minimum log level to record",
                Arrays.asList("debug", "info", "notice", "warning", "error", "critical", "alert", "emergency"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_LOGGING,
                false
        );

        // Database variables
        register(
                "DB_CONNECTION",
                "Database connection driver",
                Arrays.asList("mysql", "pgsql", "sqlite", "sqlsrv"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_DATABASE,
                false
        );

        register(
                "DB_HOST",
                "Database server host",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_DATABASE,
                false
        );

        register(
                "DB_PORT",
                "Database server port",
                null,
                EnvVariableDefinition.VariableType.INTEGER,
                GROUP_DATABASE,
                false
        );

        register(
                "DB_DATABASE",
                "Name of the database",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_DATABASE,
                false
        );

        register(
                "DB_USERNAME",
                "Database username",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_DATABASE,
                false
        );

        register(
                "DB_PASSWORD",
                "Database password",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_DATABASE,
                true
        );

        // Broadcast, Cache, Queue variables
        register(
                "BROADCAST_DRIVER",
                "Broadcast driver for real-time events",
                Arrays.asList("pusher", "redis", "log", "null"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_BROADCAST,
                false
        );

        register(
                "CACHE_DRIVER",
                "Cache system used by the application",
                Arrays.asList("file", "database", "redis", "memcached", "array"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_CACHE,
                false
        );

        register(
                "QUEUE_CONNECTION",
                "Queue backend connection name",
                Arrays.asList("sync", "database", "redis", "sqs"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_QUEUE,
                false
        );

        // Session variables
        register(
                "SESSION_DRIVER",
                "Session storage mechanism",
                Arrays.asList("file", "cookie", "database", "redis", "array"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_SESSION,
                false
        );

        register(
                "SESSION_LIFETIME",
                "Number of minutes that sessions are allowed to remain idle",
                null,
                EnvVariableDefinition.VariableType.INTEGER,
                GROUP_SESSION,
                false
        );

        // Mail variables
        register(
                "MAIL_MAILER",
                "Mail sending driver",
                Arrays.asList("smtp", "sendmail", "mailgun", "ses", "postmark", "log", "array"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_MAIL,
                false
        );

        register(
                "MAIL_HOST",
                "SMTP server hostname",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_MAIL,
                false
        );

        register(
                "MAIL_PORT",
                "SMTP server port",
                null,
                EnvVariableDefinition.VariableType.INTEGER,
                GROUP_MAIL,
                false
        );

        register(
                "MAIL_USERNAME",
                "SMTP username",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_MAIL,
                false
        );

        register(
                "MAIL_PASSWORD",
                "SMTP password",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_MAIL,
                true
        );

        register(
                "MAIL_ENCRYPTION",
                "Encryption protocol for mail",
                Arrays.asList("ssl", "tls", ""),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_MAIL,
                false
        );

        register(
                "MAIL_FROM_ADDRESS",
                "Email address used as sender",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_MAIL,
                false
        );

        register(
                "MAIL_FROM_NAME",
                "Sender name for emails",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_MAIL,
                false
        );

        // Pusher variables
        register(
                "PUSHER_APP_ID",
                "Pusher app ID for broadcasting",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_PUSHER,
                false
        );

        register(
                "PUSHER_APP_KEY",
                "Pusher app key",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_PUSHER,
                false
        );

        register(
                "PUSHER_APP_SECRET",
                "Pusher app secret",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_PUSHER,
                true
        );

        register(
                "PUSHER_APP_CLUSTER",
                "Pusher cluster location",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_PUSHER,
                false
        );

        // AWS variables
        register(
                "AWS_ACCESS_KEY_ID",
                "AWS access key",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_AWS,
                false
        );

        register(
                "AWS_SECRET_ACCESS_KEY",
                "AWS secret key",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_AWS,
                true
        );

        register(
                "AWS_DEFAULT_REGION",
                "AWS region",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_AWS,
                false
        );

        register(
                "AWS_BUCKET",
                "S3 bucket name",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_AWS,
                false
        );

        // Vite Pusher variables
        register(
                "VITE_PUSHER_APP_KEY",
                "For frontend tooling with Vite using Pusher",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_VITE_PUSHER,
                true
        );

        register(
                "VITE_PUSHER_HOST",
                "Host for Pusher, often localhost or remote",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_VITE_PUSHER,
                false
        );

        register(
                "VITE_PUSHER_PORT",
                "Port for Pusher",
                null,
                EnvVariableDefinition.VariableType.INTEGER,
                GROUP_VITE_PUSHER,
                false
        );

        register(
                "VITE_PUSHER_SCHEME",
                "Connection scheme",
                Arrays.asList("http", "https"),
                EnvVariableDefinition.VariableType.DROPDOWN,
                GROUP_VITE_PUSHER,
                false
        );

        register(
                "VITE_PUSHER_APP_CLUSTER",
                "Pusher cluster, e.g., 'mt1'",
                null,
                EnvVariableDefinition.VariableType.STRING,
                GROUP_VITE_PUSHER,
                false
        );
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

    @Override
    public List<EnvFileDefinition> getEnvFileDefinitions() {
        List<EnvFileDefinition> definitions = new ArrayList<>();
        
        // Primary .env file
        definitions.add(EnvFileDefinition.createPrimaryEnv());
        
        // Template file
        definitions.add(EnvFileDefinition.createEnvExample());
        
        // Testing environment
        definitions.add(EnvFileDefinition.createTestingEnv());
        
        // Local overrides
        definitions.add(EnvFileDefinition.createLocalEnv());
        
        // Production environment
        definitions.add(EnvFileDefinition.createProductionEnv());
        
        return definitions;
    }


    @Override
    public List<CliActionDefinition> getAvailableCliActions() {

        CliActionDefinition[] definitions = {
            new CliActionDefinition(
                "artisan_key_generate",
                "Generate Application Key",
                "php artisan key:generate",
                "Generate a new application key and store it in the .env file"
            ).addEnvironmentVariable("ENV_FILE", "{selectedEnvFile}"),

            new CliActionDefinition(
                "artisan_env_encrypt",
                "Encrypt Environment File",
                "php artisan env:encrypt",
                "Encrypts an environment file variable using the Laravel framework"
            ).addParameter(
                new CliParameterDefinition(
                    "name",
                    "Variable Name",
                    "Name of the environment variable to retrieve",
                    true
                )
            ),
        };
        return Arrays.asList(definitions);
    }

    @Override
    public boolean supportsTemplateFiles() {
        return true;
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
