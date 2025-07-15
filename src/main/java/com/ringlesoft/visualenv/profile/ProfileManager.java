package com.ringlesoft.visualenv.profile;

import com.intellij.openapi.project.Project;
import com.ringlesoft.visualenv.utils.ProjectDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages environment variable profiles and selects the appropriate profile for a project.
 */
public class ProfileManager {
    private static final List<EnvProfile> AVAILABLE_PROFILES = new ArrayList<>();
    
    static {
        // Register all available profiles
        AVAILABLE_PROFILES.add(new LaravelProfile());
        AVAILABLE_PROFILES.add(new NodeJSProfile());
        AVAILABLE_PROFILES.add(new GenericProfile());
        // More profiles can be added here
    }
    
    /**
     * Gets the appropriate profile for a project based on project type detection.
     *
     * @param project The project to get a profile for
     * @return The most appropriate profile for the project
     */
    public static EnvProfile getProfileForProject(Project project) {
        // Try to detect which profile matches the project
        if (ProjectDetector.isLaravelProject(project)) {
            return getProfileByName("Laravel");
        } else if (ProjectDetector.isNodeJSProject(project)) {
            return getProfileByName("NodeJS");
        } else if (ProjectDetector.isDjangoProject(project)) {
            return getProfileByName("Django"); // This will return the generic profile since we don't have a Django profile yet
        }
        // Add more project type detections here
        
        // Default to a generic profile if no specific match
        return new GenericProfile();
    }
    
    /**
     * Gets a profile by its name.
     *
     * @param name The name of the profile to get
     * @return The profile with the given name, or a GenericProfile if not found
     */
    public static EnvProfile getProfileByName(String name) {
        return AVAILABLE_PROFILES.stream()
                .filter(p -> p.getProfileName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(new GenericProfile());
    }
    
    /**
     * Gets all available profiles.
     *
     * @return List of all registered profiles
     */
    public static List<EnvProfile> getAllProfiles() {
        return Collections.unmodifiableList(AVAILABLE_PROFILES);
    }
}
