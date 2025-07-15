package com.ringlesoft.visualenv.model;

/**
 * Represents an environment variable.
 */
public class EnvVariable {
    private final String name;
    private final String value;
    private final String source;
    private final boolean isSecret;

    public EnvVariable(String name, String value, String source) {
        this(name, value, source, false);
    }

    public EnvVariable(String name, String value, String source, boolean isSecret) {
        this.name = name;
        this.value = value;
        this.source = source;
        this.isSecret = isSecret;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return isSecret ? "********" : value;
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

    @Override
    public String toString() {
        return name + "=" + getValue();
    }
}
