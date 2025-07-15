package com.ringlesoft.visualenv.model;

import java.util.*;

/**
 * Registry containing predefined environment variables with their descriptions, possible values, and groups.
 * Based on common Laravel environment variables.
 */
public class EnvVariableRegistry {
    private static final Map<String, EnvVariableDefinition> REGISTRY = new HashMap<>();
    
    // Groups
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

    static {
        // App variables
        register(
            "APP_NAME", 
            "The name of your application", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_APP
        );
        
        register(
            "APP_ENV", 
            "The application environment", 
            Arrays.asList("local", "production", "staging", "testing"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_APP
        );
        
        register(
            "APP_KEY", 
            "Application encryption key used for encryption and sessions", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_APP
        );
        
        register(
            "APP_DEBUG", 
            "Enables debug mode for detailed error messages", 
            Arrays.asList("true", "false"), 
            EnvVariableDefinition.VariableType.BOOLEAN, 
            GROUP_APP
        );
        
        register(
            "APP_URL", 
            "The URL of your application", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_APP
        );
        
        // Logging variables
        register(
            "LOG_CHANNEL", 
            "Specifies the default logging channel", 
            Arrays.asList("stack", "single", "daily", "slack"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_LOGGING
        );
        
        register(
            "LOG_LEVEL", 
            "Minimum log level to record", 
            Arrays.asList("debug", "info", "notice", "warning", "error", "critical", "alert", "emergency"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_LOGGING
        );
        
        // Database variables
        register(
            "DB_CONNECTION", 
            "Database connection driver", 
            Arrays.asList("mysql", "pgsql", "sqlite", "sqlsrv"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_DATABASE
        );
        
        register(
            "DB_HOST", 
            "Database server host", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_DATABASE
        );
        
        register(
            "DB_PORT", 
            "Database server port", 
            null, 
            EnvVariableDefinition.VariableType.INTEGER, 
            GROUP_DATABASE
        );
        
        register(
            "DB_DATABASE", 
            "Name of the database", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_DATABASE
        );
        
        register(
            "DB_USERNAME", 
            "Database username", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_DATABASE
        );
        
        register(
            "DB_PASSWORD", 
            "Database password", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_DATABASE
        );
        
        // Broadcast, Cache, Queue variables
        register(
            "BROADCAST_DRIVER", 
            "Broadcast driver for real-time events", 
            Arrays.asList("pusher", "redis", "log", "null"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_BROADCAST
        );
        
        register(
            "CACHE_DRIVER", 
            "Cache system used by the application", 
            Arrays.asList("file", "database", "redis", "memcached", "array"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_CACHE
        );
        
        register(
            "QUEUE_CONNECTION", 
            "Queue backend connection name", 
            Arrays.asList("sync", "database", "redis", "sqs"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_QUEUE
        );
        
        // Session variables
        register(
            "SESSION_DRIVER", 
            "Session storage mechanism", 
            Arrays.asList("file", "cookie", "database", "redis", "array"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_SESSION
        );
        
        register(
            "SESSION_LIFETIME", 
            "Number of minutes that sessions are allowed to remain idle", 
            null, 
            EnvVariableDefinition.VariableType.INTEGER, 
            GROUP_SESSION
        );
        
        // Mail variables
        register(
            "MAIL_MAILER", 
            "Mail sending driver", 
            Arrays.asList("smtp", "sendmail", "mailgun", "ses", "postmark", "log", "array"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_HOST", 
            "SMTP server hostname", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_PORT", 
            "SMTP server port", 
            null, 
            EnvVariableDefinition.VariableType.INTEGER, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_USERNAME", 
            "SMTP username", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_PASSWORD", 
            "SMTP password", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_ENCRYPTION", 
            "Encryption protocol for mail", 
            Arrays.asList("ssl", "tls", ""), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_FROM_ADDRESS", 
            "Email address used as sender", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_MAIL
        );
        
        register(
            "MAIL_FROM_NAME", 
            "Sender name for emails", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_MAIL
        );
        
        // Pusher variables
        register(
            "PUSHER_APP_ID", 
            "Pusher app ID for broadcasting", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_PUSHER
        );
        
        register(
            "PUSHER_APP_KEY", 
            "Pusher app key", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_PUSHER
        );
        
        register(
            "PUSHER_APP_SECRET", 
            "Pusher app secret", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_PUSHER
        );
        
        register(
            "PUSHER_APP_CLUSTER", 
            "Pusher cluster location", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_PUSHER
        );
        
        // AWS variables
        register(
            "AWS_ACCESS_KEY_ID", 
            "AWS access key", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_AWS
        );
        
        register(
            "AWS_SECRET_ACCESS_KEY", 
            "AWS secret key", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_AWS
        );
        
        register(
            "AWS_DEFAULT_REGION", 
            "AWS region", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_AWS
        );
        
        register(
            "AWS_BUCKET", 
            "S3 bucket name", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_AWS
        );
        
        // Vite Pusher variables
        register(
            "VITE_PUSHER_APP_KEY", 
            "For frontend tooling with Vite using Pusher", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_VITE_PUSHER
        );
        
        register(
            "VITE_PUSHER_HOST", 
            "Host for Pusher, often localhost or remote", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_VITE_PUSHER
        );
        
        register(
            "VITE_PUSHER_PORT", 
            "Port for Pusher", 
            null, 
            EnvVariableDefinition.VariableType.INTEGER, 
            GROUP_VITE_PUSHER
        );
        
        register(
            "VITE_PUSHER_SCHEME", 
            "Connection scheme", 
            Arrays.asList("http", "https"), 
            EnvVariableDefinition.VariableType.DROPDOWN, 
            GROUP_VITE_PUSHER
        );
        
        register(
            "VITE_PUSHER_APP_CLUSTER", 
            "Pusher cluster, e.g., 'mt1'", 
            null, 
            EnvVariableDefinition.VariableType.STRING, 
            GROUP_VITE_PUSHER
        );
    }

    private static void register(String name, String description, List<String> possibleValues, 
                                EnvVariableDefinition.VariableType type, String group) {
        EnvVariableDefinition definition = new EnvVariableDefinition(name, description, possibleValues, type, group);
        REGISTRY.put(name, definition);
    }

    /**
     * Gets a predefined environment variable definition by name.
     *
     * @param name Name of the environment variable
     * @return The variable definition, or null if not found
     */
    public static EnvVariableDefinition getDefinition(String name) {
        // TODO: Find a way to automatically register unknown variables
        return REGISTRY.get(name);
    }

    /**
     * Gets all predefined environment variable definitions.
     *
     * @return Map of variable names to definitions
     */
    public static Map<String, EnvVariableDefinition> getAllDefinitions() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    /**
     * Gets all predefined environment variable definitions for a specific group.
     *
     * @param group The group name
     * @return List of variable definitions for the group
     */
    public static List<EnvVariableDefinition> getDefinitionsForGroup(String group) {
        List<EnvVariableDefinition> result = new ArrayList<>();
        
        for (EnvVariableDefinition definition : REGISTRY.values()) {
            if (group.equals(definition.getGroup())) {
                result.add(definition);
            }
        }
        
        return result;
    }

    /**
     * Gets all available group names.
     *
     * @return Set of group names
     */
    public static Set<String> getAllGroups() {
        Set<String> groups = new HashSet<>();
        
        for (EnvVariableDefinition definition : REGISTRY.values()) {
            groups.add(definition.getGroup());
        }
        
        return Collections.unmodifiableSet(groups);
    }

    /**
     * Checks if a variable name is predefined.
     *
     * @param name Name of the environment variable
     * @return true if the variable is predefined
     */
    public static boolean isPredefined(String name) {
        return REGISTRY.containsKey(name);
    }
}
