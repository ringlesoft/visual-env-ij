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

    public EnvVariableDefinition(String name, String description, List<String> possibleValues, 
                                VariableType type, String group) {
        this.name = name;
        this.description = description;
        this.possibleValues = possibleValues != null ? possibleValues : Collections.emptyList();
        this.type = type;
        this.group = group;
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

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

    public boolean isSecret() {
        return EnvVariableRegistry.getDefinition(name).isSecret();
    }

}
