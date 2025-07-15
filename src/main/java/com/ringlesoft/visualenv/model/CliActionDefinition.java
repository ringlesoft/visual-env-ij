package com.ringlesoft.visualenv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a CLI action/command that can be executed by the plugin
 * for environment variable management related tasks.
 */
public class CliActionDefinition {
    private String id;
    private String name;
    private String command;
    private String description;
    private String category;
    private List<CliParameterDefinition> parameters;
    private Map<String, String> environmentVariables;
    private boolean requiresUserInput;
    private boolean showOutput;
    private String workingDirectory;
    
    /**
     * Creates a new CLI Action Definition
     */
    public CliActionDefinition() {
        this.parameters = new ArrayList<>();
        this.environmentVariables = new HashMap<>();
        this.requiresUserInput = false;
        this.showOutput = true;
        this.workingDirectory = "";
    }
    
    /**
     * Creates a new CLI Action Definition with basic properties
     * 
     * @param id Unique identifier for this action
     * @param name Display name for this action
     * @param command The command template to execute
     * @param description A description of what this command does
     */
    public CliActionDefinition(String id, String name, String command, String description) {
        this();
        this.id = id;
        this.name = name;
        this.command = command;
        this.description = description;
    }
    
    /**
     * Creates a new CLI Action Definition with all properties
     * 
     * @param id Unique identifier for this action
     * @param name Display name for this action
     * @param command The command template to execute
     * @param description A description of what this command does
     * @param category The category or group this command belongs to
     * @param requiresUserInput Whether this command requires user input
     * @param showOutput Whether to show command output to the user
     * @param workingDirectory Optional working directory for the command
     */
    public CliActionDefinition(String id, String name, String command, String description, 
                              String category, boolean requiresUserInput, 
                              boolean showOutput, String workingDirectory) {
        this(id, name, command, description);
        this.category = category;
        this.requiresUserInput = requiresUserInput;
        this.showOutput = showOutput;
        this.workingDirectory = workingDirectory;
    }
    
    /**
     * Build the complete command string with parameters
     * 
     * @param paramValues Map of parameter values by parameter id
     * @return The full command to execute
     */
    public String buildCommandString(Map<String, String> paramValues) {
        String result = command;
        
        // Replace parameter placeholders in the command string
        if (paramValues != null) {
            for (Map.Entry<String, String> entry : paramValues.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, entry.getValue());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Add a parameter definition to this command
     * 
     * @param param The parameter definition to add
     * @return This CLI action definition for chaining
     */
    public CliActionDefinition addParameter(CliParameterDefinition param) {
        parameters.add(param);
        return this;
    }
    
    /**
     * Add an environment variable that should be set when executing this command
     * 
     * @param name The name of the environment variable
     * @param value The value of the environment variable
     * @return This CLI action definition for chaining
     */
    public CliActionDefinition addEnvironmentVariable(String name, String value) {
        environmentVariables.put(name, value);
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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<CliParameterDefinition> getParameters() {
        return parameters;
    }

    public void setParameters(List<CliParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public boolean isRequiresUserInput() {
        return requiresUserInput;
    }

    public CliActionDefinition setRequiresUserInput(boolean requiresUserInput) {
        this.requiresUserInput = requiresUserInput;
        return this;
    }

    public boolean isShowOutput() {
        return showOutput;
    }

    public void setShowOutput(boolean showOutput) {
        this.showOutput = showOutput;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
