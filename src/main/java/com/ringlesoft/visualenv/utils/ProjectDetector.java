package com.ringlesoft.visualenv.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Utility class to detect project types
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
     * Checks if the given project is a NodeJS project
     * 
     * @param project The project to check
     * @return true if the project is a NodeJS project, false otherwise
     */
    public static boolean isNodeJSProject(Project project) {
        if (project == null) return false;
        
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return false;
        
        // Check for package.json
        VirtualFile packageJson = baseDir.findChild("package.json");
        if (packageJson != null && !packageJson.isDirectory()) {
            return true;
        }
        
        // Check for node_modules directory
        VirtualFile nodeModules = baseDir.findChild("node_modules");
        if (nodeModules != null && nodeModules.isDirectory()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if the given project is a Django project
     * 
     * @param project The project to check
     * @return true if the project is a Django project, false otherwise
     */
    public static boolean isDjangoProject(Project project) {
        if (project == null) return false;
        
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return false;
        
        // Check for manage.py
        VirtualFile managePy = baseDir.findChild("manage.py");
        if (managePy != null && !managePy.isDirectory()) {
            return true;
        }
        
        // Check for requirements.txt with Django
        VirtualFile requirementsTxt = baseDir.findChild("requirements.txt");
        if (requirementsTxt != null) {
            try {
                String content = new String(requirementsTxt.contentsToByteArray());
                if (content.contains("django") || content.contains("Django")) {
                    return true;
                }
            } catch (Exception ignored) {
                // Ignore exceptions reading the file
            }
        }
        
        // Check for typical Django directories
        return hasDjangoDirectoryStructure(baseDir);
    }
    
    /**
     * Gets the detected project type as a string
     * 
     * @param project The project to check
     * @return The detected project type name, or "Generic" if no specific type is detected
     */
    public static String getProjectType(Project project) {
        if (isLaravelProject(project)) {
            return "Laravel";
        } else if (isNodeJSProject(project)) {
            return "NodeJS";
        } else if (isDjangoProject(project)) {
            return "Django";
        }
        return "Generic";
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
    
    /**
     * Checks if the project has a Django directory structure
     *
     * @param baseDir The base directory of the project
     * @return true if the directory structure matches Django
     */
    private static boolean hasDjangoDirectoryStructure(VirtualFile baseDir) {
        // Look for Django app with migrations and templates
        for (VirtualFile child : baseDir.getChildren()) {
            if (child.isDirectory()) {
                VirtualFile migrations = child.findChild("migrations");
                VirtualFile templates = child.findChild("templates");
                VirtualFile views = child.findChild("views.py");
                VirtualFile models = child.findChild("models.py");
                
                if (migrations != null && migrations.isDirectory() && 
                    ((templates != null && templates.isDirectory()) || 
                    (views != null && !views.isDirectory()) || 
                    (models != null && !models.isDirectory()))) {
                    return true;
                }
            }
        }
        return false;
    }
}
