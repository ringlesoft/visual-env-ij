package com.ringlesoft.visualenv.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Utility class to detect if a project is a Laravel project
 */
public class ProjectDetector {
    
    /**
     * Checks if the given project is a Laravel project
     *
     * @param project The project to check
     * @return true if the project is a Laravel project, false otherwise
     */
    public static boolean isLaravelProject(Project project) {
        if (project == null) return false;
        
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return false;
        
        // Check for artisan file (Laravel's command-line tool)
        VirtualFile artisanFile = baseDir.findChild("artisan");
        if (artisanFile != null && !artisanFile.isDirectory()) {
            return true;
        }
        
        // Check for composer.json with Laravel framework
        VirtualFile composerJson = baseDir.findChild("composer.json");
        if (composerJson != null) {
            return checkComposerForLaravel(composerJson);
        }
        
        // Check for typical Laravel directories
        return hasLaravelDirectoryStructure(baseDir);
    }
    
    /**
     * Checks if composer.json contains Laravel dependencies
     *
     * @param composerJson The composer.json file
     * @return true if Laravel dependencies are found
     */
    private static boolean checkComposerForLaravel(VirtualFile composerJson) {
        try {
            String content = new String(composerJson.contentsToByteArray());
            // Check for Laravel framework or Laravel project template
            return content.contains("laravel/framework") || 
                   content.contains("laravel/laravel");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if the project has a Laravel directory structure
     *
     * @param baseDir The base directory of the project
     * @return true if the directory structure matches Laravel
     */
    private static boolean hasLaravelDirectoryStructure(VirtualFile baseDir) {
        // Check for typical Laravel directories
        String[] laravelDirs = {"app", "bootstrap", "config", "database", "resources", "routes"};
        int foundDirs = 0;
        
        for (String dirName : laravelDirs) {
            VirtualFile dir = baseDir.findChild(dirName);
            if (dir != null && dir.isDirectory()) {
                foundDirs++;
            }
        }
        
        // If we find most Laravel directories, it's likely a Laravel project
        return foundDirs >= 4;
    }
}
