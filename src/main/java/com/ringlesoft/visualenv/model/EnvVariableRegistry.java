package com.ringlesoft.visualenv.model;

import com.intellij.openapi.project.Project;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.profile.GenericProfile;

import java.util.*;

/**
 * Registry for environment variables that adapts to the currently selected profile.
 * Delegates to the active profile to get variable definitions, groups, and other metadata.
 */
public class EnvVariableRegistry {
    private EnvProfile activeProfile;
    private Project project;

    /**
     * Creates a new EnvVariableRegistry with a default generic profile.
     */
    public EnvVariableRegistry() {
        this.activeProfile = new GenericProfile();
    }
    
    /**
     * Creates a new EnvVariableRegistry with the specified profile.
     * 
     * @param profile The profile to use for variable definitions
     */
    public EnvVariableRegistry(EnvProfile profile) {
        this.activeProfile = profile != null ? profile : new GenericProfile();
    }
    
    /**
     * Creates a new EnvVariableRegistry with the specified profile and project.
     * 
     * @param profile The profile to use for variable definitions
     * @param project The current project
     */
    public EnvVariableRegistry(EnvProfile profile, Project project) {
        this.activeProfile = profile != null ? profile : new GenericProfile();
        this.project = project;
    }

    /**
     * Gets all registered environment variables.
     * 
     * @return Map of variable name to variable definition
     */
    public Map<String, EnvVariableDefinition> getRegisteredVariables() {
        return activeProfile.getDefinitions();
    }

    /**
     * Gets the definition for a specific environment variable.
     * 
     * @param name Name of the environment variable
     * @return Definition of the variable, or null if not registered
     */
    public EnvVariableDefinition getVariableDefinition(String name) {
        return activeProfile.getDefinition(name);
    }
    
    /**
     * Gets the group for a specific environment variable.
     * 
     * @param name Name of the environment variable
     * @return Group of the variable, or "other" if not registered
     */
    public String getVariableGroup(String name) {
        EnvVariableDefinition definition = activeProfile.getDefinition(name);
        return definition != null ? definition.getGroup() : "other";
    }
    
    /**
     * Gets all variables for a specific group.
     * 
     * @param group Group name
     * @return List of variable definitions in the group
     */
    public List<EnvVariableDefinition> getVariablesForGroup(String group) {
        return activeProfile.getDefinitionsForGroup(group);
    }
    
    /**
     * Gets all available variable groups.
     * 
     * @return Set of group names
     */
    public Set<String> getAllGroups() {
        return activeProfile.getAllGroups();
    }
    
    /**
     * Checks if a variable name should be treated as a secret.
     * Uses naming patterns to detect sensitive information.
     * 
     * @param name Name of the environment variable
     * @return true if the variable should be treated as a secret
     */
    public boolean detectSecretVariable(String name) {
        // First check if it's defined in the profile
        EnvVariableDefinition definition = activeProfile.getDefinition(name);
        if (definition != null) {
            return definition.isSecret();
        }
        
        // Otherwise use heuristics
        String upperName = name.toUpperCase();
        return upperName.contains("KEY") ||
               upperName.contains("SECRET") ||
               upperName.contains("PASSWORD") ||
               upperName.contains("TOKEN") ||
               upperName.contains("SIGNATURE") ||
               upperName.contains("CERT");
    }
    
    /**
     * Sets the active profile for this registry.
     * 
     * @param profile Profile to use
     */
    public void setActiveProfile(EnvProfile profile) {
        if (profile != null) {
            this.activeProfile = profile;
        }
    }
    
    /**
     * Gets the active profile.
     * 
     * @return The current active profile
     */
    public EnvProfile getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Checks if a variable is predefined in the active profile.
     * 
     * @param name Variable name
     * @return true if the variable is predefined
     */
    public boolean isVariablePredefined(String name) {
        return activeProfile.isVariablePredefined(name);
    }
    
    /**
     * Gets the project associated with this registry.
     * 
     * @return The project
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Sets the project associated with this registry.
     * 
     * @param project The project
     */
    public void setProject(Project project) {
        this.project = project;
    }
}
