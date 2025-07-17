package com.ringlesoft.visualenv.model;

/**
 * Represents an environment variable.
 */
public class EnvVariable {
    private final String name;
    private final String value;
    private final String source;
    private final boolean isSecret;
    private final String group;

    public EnvVariable(String name, String value, String source) {
        this(name, value, source, false, "other");
    }

    public EnvVariable(String name, String value, String source, boolean isSecret) {
        this(name, value, source, isSecret, "other");
    }

    public EnvVariable(String name, String value, String source, boolean isSecret, String group) {
        this.name = name;
        this.value = value;
        this.source = source;
        this.isSecret = isSecret;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return isSecret ? "*".repeat(value.length()) : value;
    }

    public String getRawValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public String getGroup() {
        return group;
    }

    public boolean hasInterpolation() {
        return value.contains("${") || value.contains("$(");
    }

    @Override
    public String toString() {
        return name + "=" + getValue();
    }
}
