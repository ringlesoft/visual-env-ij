package com.ringlesoft.visualenv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a parameter for a CLI action/command
 */
public class CliParameterDefinition {
    private String id;
    private String name;
    private String description;
    private String defaultValue;
    private boolean required;
    private List<String> allowedValues;
    private ParameterType parameterType;
    
    /**
     * Parameter type enum
     */
    public enum ParameterType {
        STRING,
        NUMBER,
        BOOLEAN,
        FILE_PATH,
        ENUM
    }
    
    /**
     * Creates a new CLI Parameter Definition
     */
    public CliParameterDefinition() {
        this.allowedValues = new ArrayList<>();
        this.required = false;
        this.parameterType = ParameterType.STRING;
    }
    
    /**
     * Creates a new CLI Parameter Definition with basic properties
     * 
     * @param id Unique identifier for this parameter
     * @param name Display name for this parameter
     * @param description A description of what this parameter does
     * @param required Whether this parameter is required
     */
    public CliParameterDefinition(String id, String name, String description, boolean required) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.required = required;
    }
    
    /**
     * Creates a new CLI Parameter Definition with all properties
     * 
     * @param id Unique identifier for this parameter
     * @param name Display name for this parameter
     * @param description A description of what this parameter does
     * @param defaultValue The default value for this parameter
     * @param required Whether this parameter is required
     * @param parameterType The type of this parameter
     */
    public CliParameterDefinition(String id, String name, String description,
                                String defaultValue, boolean required, ParameterType parameterType) {
        this(id, name, description, required);
        this.defaultValue = defaultValue;
        this.parameterType = parameterType;
    }
    
    /**
     * Add an allowed value for this parameter (used for ENUM type)
     * 
     * @param value The allowed value
     * @return This CLI parameter definition for chaining
     */
    public CliParameterDefinition addAllowedValue(String value) {
        allowedValues.add(value);
        return this;
    }

    // Getters and setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }
}
