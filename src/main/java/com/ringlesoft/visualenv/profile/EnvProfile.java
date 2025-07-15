package com.ringlesoft.visualenv.profile;

import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for environment variable profiles.
 * A profile defines the environment variables and their characteristics for a specific project type.
 */
public interface EnvProfile {
    /**
     * Gets the name of the profile.
     *
     * @return Profile name
     */
    String getProfileName();
    
    /**
     * Gets a description of the profile.
     *
     * @return Profile description
     */
    String getProfileDescription();
    
    /**
     * Gets all environment variable definitions for this profile.
     *
     * @return Map of variable name to definition
     */
    Map<String, EnvVariableDefinition> getDefinitions();
    
    /**
     * Gets the definition for a specific environment variable.
     *
     * @param name Name of the environment variable
     * @return Definition of the variable, or null if not found
     */
    EnvVariableDefinition getDefinition(String name);
    
    /**
     * Gets all environment variable definitions for a specific group.
     *
     * @param group Group name
     * @return List of definitions in the group
     */
    List<EnvVariableDefinition> getDefinitionsForGroup(String group);
    
    /**
     * Gets all group names defined in this profile.
     *
     * @return Set of group names
     */
    Set<String> getAllGroups();
    
    /**
     * Checks if an environment variable is predefined in this profile.
     *
     * @param name Name of the variable to check
     * @return true if the variable is predefined, false otherwise
     */
    boolean isVariablePredefined(String name);
    
    /**
     * Checks if this profile supports Artisan commands (for Laravel projects).
     *
     * @return true if Artisan commands are supported, false otherwise
     */
    boolean supportsArtisanCommands();

    /**
     * Get common environment file names for this profile
     *
     * @return Array of common file names (e.g., .env, .env.example)
     */
    String[] getCommonEnvFiles();

    /**
     * Get environment file definitions for this profile.
     * This method returns a list of environment file definitions with detailed information
     * about each file's purpose, editability, and priority.
     *
     * @return List of environment file definitions
     */
    List<EnvFileDefinition> getEnvFileDefinitions();

    boolean supportsTemplateFiles();
}
