package com.ringlesoft.visualenv.model;

/**
 * Defines different types of environment files and their characteristics.
 * This helps in categorizing and handling different .env file variants
 * based on their purpose and profile (project type).
 */
public class EnvFileDefinition {
    private final String name;
    private final String description;
    private final boolean isTemplate;
    private final boolean isEditable;
    private final int priority;
    private final EnvFileType fileType;

    /**
     * Enum representing different types of environment files
     */
    public enum EnvFileType {
        /**
         * Main environment file with actual values for local development
         */
        PRIMARY,

        /**
         * Example or template file with placeholder values
         */
        TEMPLATE,

        /**
         * Environment file for testing environments
         */
        TESTING,

        /**
         * Environment file for production deployment
         */
        PRODUCTION,

        /**
         * Local overrides that should not be committed
         */
        LOCAL_OVERRIDE,

        /**
         * Environment file for staging deployment
         */
        STAGING,

        /**
         * Environment file for development
         */
        DEVELOPMENT,

        /**
         * Custom or user-defined environment file
         */
        CUSTOM
    }

    /**
     * Constructor for EnvFileDefinition
     *
     * @param name        Name of the environment file (e.g., ".env", ".env.example")
     * @param description Description of the file's purpose
     * @param isTemplate  Whether this file serves as a template
     * @param isEditable  Whether this file should be editable by the plugin
     * @param priority    Loading priority (lower number = higher priority)
     * @param fileType    Type of environment file
     */
    public EnvFileDefinition(
            String name,
            String description,
            boolean isTemplate,
            boolean isEditable,
            int priority,
            EnvFileType fileType
    ) {
        this.name = name;
        this.description = description;
        this.isTemplate = isTemplate;
        this.isEditable = isEditable;
        this.priority = priority;
        this.fileType = fileType;
    }

    /**
     * Get the name of the environment file
     *
     * @return The file name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of the environment file
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this file is a template
     *
     * @return true if this is a template file, false otherwise
     */
    public boolean isTemplate() {
        return isTemplate;
    }

    /**
     * Check if this file should be editable
     *
     * @return true if this file should be editable, false otherwise
     */
    public boolean isEditable() {
        return isEditable;
    }

    public boolean isPrimary() {
        return fileType == EnvFileType.PRIMARY;
    }


    /**
     * Get the loading priority of this file
     *
     * @return The priority (lower number = higher priority)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Get the type of this environment file
     *
     * @return The file type
     */
    public EnvFileType getFileType() {
        return fileType;
    }

    /**
     * Creates a definition for a standard .env file (primary environment file)
     *
     * @return EnvFileDefinition for a primary .env file
     */
    public static EnvFileDefinition createPrimaryEnv() {
        return new EnvFileDefinition(
                ".env",
                "Primary environment file with actual values",
                false,
                true,
                1,
                EnvFileType.PRIMARY
        );
    }

    /**
     * Creates a definition for a template .env.example file
     *
     * @return EnvFileDefinition for a template .env.example file
     */
    public static EnvFileDefinition createEnvExample() {
        return new EnvFileDefinition(
                ".env.example",
                "Template environment file with placeholder values",
                true,
                false,
                10,
                EnvFileType.TEMPLATE
        );
    }

    /**
     * Creates a definition for a testing environment file
     *
     * @return EnvFileDefinition for a testing environment file
     */
    public static EnvFileDefinition createTestingEnv() {
        return new EnvFileDefinition(
                ".env.testing",
                "Environment file for testing",
                false,
                true,
                5,
                EnvFileType.TESTING
        );
    }

    /**
     * Creates a definition for a local environment override file
     *
     * @return EnvFileDefinition for a local environment override file
     */
    public static EnvFileDefinition createLocalEnv() {
        return new EnvFileDefinition(
                ".env.local",
                "Local environment overrides (not committed to version control)",
                false,
                true,
                2,
                EnvFileType.LOCAL_OVERRIDE
        );
    }

    /**
     * Creates a definition for a production environment file
     *
     * @return EnvFileDefinition for a production environment file
     */
    public static EnvFileDefinition createProductionEnv() {
        return new EnvFileDefinition(
                ".env.production",
                "Environment file for production deployment",
                false,
                true,
                4,
                EnvFileType.PRODUCTION
        );
    }

    /**
     * Creates a definition for a development environment file
     *
     * @return EnvFileDefinition for a development environment file
     */
    public static EnvFileDefinition createDevelopmentEnv() {
        return new EnvFileDefinition(
                ".env.development",
                "Environment file for development",
                false,
                true,
                3,
                EnvFileType.DEVELOPMENT
        );
    }
    
    /**
     * Creates a definition for a custom environment file with the given name
     *
     * @param name Name of the custom environment file
     * @return EnvFileDefinition for a custom environment file
     */
    public static EnvFileDefinition createCustomEnv(String name) {
        return new EnvFileDefinition(
                name,
                "Custom environment file",
                false,
                true,
                20,
                EnvFileType.CUSTOM
        );
    }
}
