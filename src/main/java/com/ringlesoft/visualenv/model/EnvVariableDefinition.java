package com.ringlesoft.visualenv.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines the structure and possible values for known environment variables.
 */
public class EnvVariableDefinition {

    public enum VariableType {
        STRING,
        BOOLEAN,
        DROPDOWN,
        INTEGER
    }

    private final String name;
    private final String description;
    private final List<String> possibleValues;
    private final VariableType type;
    private final String group;
    private final boolean isSecret;

    /**
     * Constructor for environment variable definition
     *
     * @param name           Variable name
     * @param description    Description of the variable
     * @param possibleValues Possible values for dropdown type
     * @param type           Type of variable (string, boolean, etc.)
     * @param group          Group this variable belongs to
     * @param isSecret       Whether this variable contains sensitive data
     */
    public EnvVariableDefinition(String name, String description, List<String> possibleValues, 
                                VariableType type, String group, boolean isSecret) {
        this.name = name;
        this.description = description;
        this.possibleValues = possibleValues != null ? possibleValues : Collections.emptyList();
        this.type = type;
        this.group = group;
        this.isSecret = isSecret;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPossibleValues() {
        return Collections.unmodifiableList(possibleValues);
    }

    public VariableType getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public boolean isSecret() {
        return isSecret;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

}
