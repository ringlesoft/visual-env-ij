package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.EnvVariableDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for environment variable profiles.
 * Different profiles can provide project type-specific environment variables, groups, and behaviors.
 */
public interface EnvProfile {
    /**
     * Get the name of the profile
     * 
     * @return The profile name
     */
    String getProfileName();
    
    /**
     * Get a description of the profile
     * 
     * @return The profile description
     */
    String getProfileDescription();
    
    /**
     * Get all environment variable definitions in this profile
     * 
     * @return Map of variable names to their definitions
     */
    Map<String, EnvVariableDefinition> getDefinitions();
    
    /**
     * Get a specific environment variable definition by name
     * 
     * @param name Name of the environment variable
     * @return The variable definition, or null if not found
     */
    EnvVariableDefinition getDefinition(String name);
    
    /**
     * Get all variable definitions for a specific group
     * 
     * @param group The group name
     * @return List of variable definitions for the group
     */
    List<EnvVariableDefinition> getDefinitionsForGroup(String group);
    
    /**
     * Get all available group names
     * 
     * @return Set of group names
     */
    Set<String> getAllGroups();
    
    /**
     * Check if a variable name is predefined in this profile
     * 
     * @param name Name of the environment variable
     * @return true if the variable is predefined
     */
    boolean isVariablePredefined(String name);
    
    /**
     * Check if this profile supports Artisan commands (Laravel-specific)
     * 
     * @return true if Artisan commands are supported
     */
    boolean supportsArtisanCommands();
    
    /**
     * Get common environment file names for this profile
     * 
     * @return Array of common file names (e.g., .env, .env.example)
     */
    String[] getCommonEnvFiles();
}
