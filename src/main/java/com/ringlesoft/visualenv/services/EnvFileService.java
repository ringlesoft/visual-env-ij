package com.ringlesoft.visualenv.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.profile.ProfileManager;
import com.ringlesoft.visualenv.utils.CommandRunner;
import com.ringlesoft.visualenv.utils.EnvFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing environment variables in the project.
 */
@Service(Service.Level.PROJECT)
public final class EnvFileService {
    private static final Logger LOG = Logger.getInstance(EnvFileService.class);
    private final Project project;
    private final Map<VirtualFile, List<EnvVariable>> fileEnvVariables = new HashMap<>();
    private VirtualFile activeEnvFile;
    private EnvProfile activeProfile;
    private final EnvVariableRegistry variableRegistry;
    private String lastUpdatedVariable;

    /**
     * Create a new EnvFileService for a project
     *
     * @param project The project
     */
    public EnvFileService(Project project) {
        this.project = project;
        // Initialize the active profile based on project type
        this.activeProfile = ProfileManager.getProfileForProject(project);
        this.variableRegistry = new EnvVariableRegistry(this.activeProfile, project);
        LOG.info("EnvFileService initialized with profile: " + activeProfile.getProfileName());
    }

    /**
     * Parse an environment file and extract variables
     *
     * @param file The file to parse
     * @return List of environment variables
     */
    public List<EnvVariable> parseEnvFile(VirtualFile file) {
        List<EnvVariable> variables = new ArrayList<>();
        this.fileEnvVariables.put(file, variables);
        activeEnvFile = file;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines (for now)
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                // Match key=value pattern
                Pattern pattern = Pattern.compile("^([^=]+)=(.*)$");
                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {
                    String name = matcher.group(1).trim();
                    String value = matcher.group(2).trim();

                    // Remove quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }

                    // Check if this is a predefined variable
                    EnvVariableDefinition definition = variableRegistry.getVariableDefinition(name);

                    // Determine group
                    String group = (definition != null) ? definition.getGroup() : "other";

                    // Determine if secret
                    boolean isSecret = (definition != null) ? definition.isSecret() : variableRegistry.detectSecretVariable(name);

                    EnvVariable variable = new EnvVariable(name, value, file.getPath(), isSecret, group);
                    variables.add(variable);
                }
            }

            // Cache variables
            fileEnvVariables.put(file, variables);

            return variables;
        } catch (IOException e) {
            LOG.error("Failed to parse env file", e);
            return Collections.emptyList();
        }
    }

    /**
     * Update an environment variable in the active file
     *
     * @param name  The name of the variable
     * @param value The new value
     * @return true if successful
     */
    public boolean updateEnvVariable(String name, String value) {
        if (activeEnvFile == null) {
            LOG.error("No active env file");
            return false;
        }

        try {
            // Use EnvFileManager to update the variable
            EnvFileManager.setEnvVariable(project, activeEnvFile, name, value);
            // Update cache
            parseEnvFile(activeEnvFile);
            lastUpdatedVariable = name; // Just for keeping track
            return true;
        } catch (Exception e) {
            LOG.error("Failed to update env variable", e);
            return false;
        }
    }

    /**
     * Get all environment variables from all loaded files
     *
     * @return Map of files to their variables
     */
    public Map<String, List<EnvVariable>> getAllFileEnvVariables() {
        Map<String, List<EnvVariable>> result = new HashMap<>();
        for (Map.Entry<VirtualFile, List<EnvVariable>> entry : fileEnvVariables.entrySet()) {
            result.put(entry.getKey().getPath(), entry.getValue());
        }
        return result;
    }

    /**
     * Checks for .env files in the project root and creates one from .env.example if needed.
     *
     */
    public void scanAndProcessEnvFiles() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }
        List<VirtualFile> foundFiles = new ArrayList<>();
        ProjectService projectService = project.getService(ProjectService.class);
        List<EnvFileDefinition> envFileDefinitions = activeProfile.getEnvFileDefinitions(); // Add null check>
        for (EnvFileDefinition envFileDefinition : envFileDefinitions) {
            VirtualFile envFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, envFileDefinition.getName()).toString());
            if (envFile != null) {
                // Parse existing .env file but do it safely
                parseEnvFile(envFile);
                if (envFileDefinition.isPrimary() && activeEnvFile == null) {
                    projectService.setActiveEnvFile(envFileDefinition.getName());
                }
                foundFiles.add(envFile);
            }
        }
        if (foundFiles.isEmpty()) {
            fileEnvVariables.clear();
            activeEnvFile = null;
            projectService.setActiveEnvFile(null);
        }
    }

    public void rescanEnvFiles() {
        scanAndProcessEnvFiles();
    }


    /**
     * Create an environment file from a template file
     *
     * @param templateFile The template file (e.g., .env.example)
     * @return true if the file was successfully created, false otherwise
     */
    public boolean createEnvFromTemplate(VirtualFile templateFile) {
        if (templateFile == null || !templateFile.exists()) {
            LOG.warn("Template file does not exist");
            return false;
        }

        // Get the project directory
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            LOG.warn("Project base path is null");
            return false;
        }

        // Figure out the target file name
        String targetFileName = ".env";
        VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(projectBasePath);
        if (projectDir == null) {
            LOG.warn("Project directory not found");
            return false;
        }

        // Check if target file already exists
        VirtualFile targetFile = projectDir.findChild(targetFileName);
        if (targetFile != null && targetFile.exists()) {
            LOG.info("Target .env file already exists");
            return false; // Don't overwrite existing file
        }

        try {
            // Read the template file
            String templateContent = new String(templateFile.contentsToByteArray(), StandardCharsets.UTF_8);
            StringBuilder newContent = new StringBuilder();

            // Process each line
            for (String line : templateContent.split("\n")) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    // Keep comments and empty lines as is
                    newContent.append(line).append('\n');
                    continue;
                }

                // Check if it's a key-value line
                int equalPos = line.indexOf('=');
                if (equalPos > 0) {
                    String key = line.substring(0, equalPos).trim();
                    String value = "";
                    if (equalPos < line.length() - 1) {
                        value = line.substring(equalPos + 1).trim();
                    }

                    // Check if this is a value that should be randomized
                    EnvVariableDefinition definition = variableRegistry.getVariableDefinition(key);
                    if (definition != null && definition.isSecret()) {
                        // Generate a random string for secret values
                        value = generateRandomString();
                    } else if (value.isEmpty() || value.equals("null") ||
                            (key.toLowerCase().contains("key") && !key.toLowerCase().contains("keyboard"))) {
                        // Heuristic: if it has "key" in the name but no value, randomize it
                        value = generateRandomString();
                    }

                    newContent.append(key).append('=').append(value).append('\n');
                } else {
                    // Not a key-value line, keep as is
                    newContent.append(line).append('\n');
                }
            }

            // Create new .env file
            File newEnvFile = new File(projectBasePath, targetFileName);
            try (FileOutputStream fos = new FileOutputStream(newEnvFile)) {
                fos.write(newContent.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Refresh the file system to see the new file
            LocalFileSystem.getInstance().refreshIoFiles(Collections.singletonList(newEnvFile));

            // Load the new file
            VirtualFile newEnvVirtualFile = LocalFileSystem.getInstance().findFileByPath(newEnvFile.getAbsolutePath());
            if (newEnvVirtualFile != null) {
                // Parse and set as active env file
                parseEnvFile(newEnvVirtualFile);
                activeEnvFile = newEnvVirtualFile;
            }

            return true;
        } catch (IOException e) {
            LOG.error("Error creating env file from template", e);
            return false;
        }
    }

    /**
     * Generate a random string for use as a secret key
     *
     * @return Random string
     */
    private String generateRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 32; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }

        return result.toString();
    }


    /**
     * For backward compatibility with existing code
     */
    public boolean createEnvFromExample(VirtualFile envExampleFile) {
        return createEnvFromTemplate(envExampleFile);
    }

    /**
     * Executes an Artisan command in the Laravel project
     *
     * @param command The command to execute (without the "php artisan" prefix)
     * @return The command output
     */
    public String executeArtisanCommand(String command) {
        try {
            String basePath = project.getBasePath();
            if (basePath == null) return "Project base path not found";

            // Check if artisan file exists
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(basePath);
            if (baseDir == null) return "Project base directory not found";

            VirtualFile artisanFile = baseDir.findChild("artisan");
            if (artisanFile == null || artisanFile.isDirectory()) {
                return "Artisan file not found. This is not a Laravel project.";
            }

            CommandRunner commandRunner = new CommandRunner(project);
            return commandRunner.runCommandWithOutput(command);
        } catch (Exception e) {
            LOG.error("Error executing Artisan command", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get the currently active .env file.
     *
     * @return The active .env file, or null if none is active
     */
    public VirtualFile getActiveEnvFile() {
        return activeEnvFile;
    }

    /**
     * Get the currently active profile
     *
     * @return The active profile
     */
    public EnvProfile getActiveProfile() {
        return activeProfile;
    }


    /**
     * Sets the active profile for this service.
     * This will update the variable registry to use the new profile.
     *
     * @param profile The profile to set as active
     */
    public void setActiveProfile(EnvProfile profile) {
        if (profile != null) {
            this.activeProfile = profile;
            this.variableRegistry.setActiveProfile(profile);
            // Refresh variables to reflect new profile settings
            refreshVariables();
            LOG.info("Switched to profile: " + profile.getProfileName());
        }
    }

    /**
     * Get the variable registry for this service.
     *
     * @return The variable registry
     */
    public EnvVariableRegistry getVariableRegistry() {
        return variableRegistry;
    }

    /**
     * Get all environment file definitions for the current profile
     *
     * @return List of environment file definitions
     */
    public List<EnvFileDefinition> getEnvFileDefinitions() {
        return activeProfile.getEnvFileDefinitions();
    }

    /**
     * Get the environment variables for each file in the project
     */
    public Map<VirtualFile, List<EnvVariable>> getFileEnvVariables() {
        return fileEnvVariables;
    }


    /**
     * Find the environment file definition that matches a given file
     *
     * @param file The file to find a definition for
     * @return The matching definition, or null if none matches
     */
    public EnvFileDefinition getEnvFileDefinitionForFile(VirtualFile file) {
        if (file == null) {
            return null;
        }

        String fileName = file.getName();

        for (EnvFileDefinition definition : getEnvFileDefinitions()) {
            if (fileName.equals(definition.getName())) {
                return definition;
            }
        }

        return null;
    }

    /**
     * Check if a file is a template environment file
     *
     * @param file The file to check
     * @return true if the file is a template, false otherwise
     */
    public boolean isTemplateEnvFile(VirtualFile file) {
        EnvFileDefinition definition = getEnvFileDefinitionForFile(file);
        return definition != null && definition.isTemplate();
    }

    /**
     * Check if a file is editable by this plugin
     *
     * @param file The file to check
     * @return true if the file should be editable, false otherwise
     */
    public boolean isEditableEnvFile(VirtualFile file) {
        EnvFileDefinition definition = getEnvFileDefinitionForFile(file);
        return definition != null && definition.isEditable();
    }

    public VirtualFile findFileByPath(String selectedFilePath) {
        return LocalFileSystem.getInstance().findFileByPath(selectedFilePath);
    }

    private void refreshVariables() {
        // Refresh variables to reflect new profile settings
        if (activeEnvFile != null) {
            parseEnvFile(activeEnvFile);
        }
    }

    public String getLastUpdatedVariable() {
        return lastUpdatedVariable;
    }

    /**
     * Create a new environment variable to the currently active file
     * @param key Name of the variable
     * @param value Value of the variable
     */
    public boolean addVariable(String key, String value) {
        try {
            key = key.trim()
                    .replaceAll("\\s+", "_") // Replace spaces with underscores
                    .replaceAll("[^a-zA-Z0-9_]", "_") // Remove non-alphanumeric characters
                    .toUpperCase();
            EnvFileManager.setEnvVariable(project, activeEnvFile, key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteEnvVariable(String variableName) {
        try {
            EnvFileManager.removeEnvVariable(project, activeEnvFile, variableName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean renameVariable(String variableName, String newName) {
        try {
            newName = newName.trim()
                    .replaceAll("\\s+", "_") // Replace spaces with underscores
                    .replaceAll("[^a-zA-Z0-9_]", "_") // Remove non-alphanumeric characters
                    .toUpperCase();
            return EnvFileManager.renameEnvVariable(project, activeEnvFile, variableName, newName);
        } catch (Exception e) {
            return false;
        }
    }


//    /**
//     * Find a template file for creating a new environment file
//     *
//     * @return The template file or null if none is found
//     */
//    public VirtualFile findTemplateFile() {
//        // Get all env files by definition
//        Map<EnvFileDefinition, List<VirtualFile>> allFiles = findAllEnvFiles();
//
//        // First try to find a template file
//        for (EnvFileDefinition definition : getEnvFileDefinitions()) {
//            if (definition.isTemplate() && allFiles.containsKey(definition)) {
//                List<VirtualFile> files = allFiles.get(definition);
//                if (!files.isEmpty()) {
//                    return files.get(0);
//                }
//            }
//        }
//
//        // If no template file is found, try to use any environment file
//        for (List<VirtualFile> files : allFiles.values()) {
//            if (!files.isEmpty()) {
//                return files.get(0);
//            }
//        }
//
//        return null;
//    }


//    /**
//     * Find environment files in the project that match the provided definition
//     *
//     * @param definition The environment file definition to search for
//     * @return List of matching files
//     */
//    public List<VirtualFile> findEnvFilesByDefinition(EnvFileDefinition definition) {
//        List<VirtualFile> result = new ArrayList<>();
//        if (project.getBasePath() == null) {
//            return result;
//        }
//
//        VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
//        if (projectDir == null) {
//            return result;
//        }
//
//        // Look for the exact file name in the project root
//        VirtualFile file = projectDir.findChild(definition.getName());
//        if (file != null && !file.isDirectory()) {
//            result.add(file);
//        }
//
//        return result;
//    }

//    /**
//     * Find all environment files in the project that match any definition from the active profile
//     *
//     * @return Map of environment file definition to matching files
//     */
//    public Map<EnvFileDefinition, List<VirtualFile>> findAllEnvFiles() {
//        Map<EnvFileDefinition, List<VirtualFile>> result = new LinkedHashMap<>();
//
//        // Get all environment file definitions from the active profile
//        List<EnvFileDefinition> definitions = getEnvFileDefinitions();
//
//        // Sort definitions by priority (important for UI display order)
//        definitions.sort(Comparator.comparingInt(EnvFileDefinition::getPriority));
//
//        // Find files for each definition
//        for (EnvFileDefinition definition : definitions) {
//            List<VirtualFile> files = findEnvFilesByDefinition(definition);
//            if (!files.isEmpty()) {
//                result.put(definition, files);
//            }
//        }
//
//        // Also look for any .env* files that might not be in the definitions
//        if (project.getBasePath() != null) {
//            VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
//            if (projectDir != null) {
//                for (VirtualFile file : projectDir.getChildren()) {
//                    if (!file.isDirectory() && file.getName().startsWith(".env")) {
//                        boolean alreadyAdded = false;
//
//                        // Check if this file is already included
//                        for (List<VirtualFile> existingFiles : result.values()) {
//                            if (existingFiles.contains(file)) {
//                                alreadyAdded = true;
//                                break;
//                            }
//                        }
//
//                        // Add as a custom env file if not already included
//                        if (!alreadyAdded) {
//                            EnvFileDefinition customDef = EnvFileDefinition.createCustomEnv(file.getName());
//                            List<VirtualFile> customFiles = new ArrayList<>();
//                            customFiles.add(file);
//                            result.put(customDef, customFiles);
//                        }
//                    }
//                }
//            }
//        }
//
//        return result;
//    }

//    /**
//     * Get a display name for an environment file, including its type
//     *
//     * @param file The file
//     * @return Display name with type information
//     */
//    public String getEnvFileDisplayName(VirtualFile file) {
//        if (file == null) {
//            return "No file selected";
//        }
//        String fileName = file.getName();
//        EnvFileDefinition definition = getEnvFileDefinitionForFile(file);
//        if (definition != null) {
//            return fileName + " (" + definition.getDescription() + ")";
//        }
//        return fileName;
//    }


//    /**
//     * Create a new blank environment file
//     *
//     * @param fileName Name of the file to create
//     * @return true if the file was successfully created, false otherwise
//     */
//    public boolean createBlankEnvFile(String fileName) {
//        String projectBasePath = project.getBasePath();
//        if (projectBasePath == null) {
//            return false;
//        }
//
//        // Ensure the filename starts with .env
//        if (!fileName.startsWith(".env")) {
//            fileName = ".env" + fileName;
//        }
//
//        File newFile = new File(projectBasePath, fileName);
//        if (newFile.exists()) {
//            return false; // Don't overwrite existing file
//        }
//
//        try {
//            boolean created = newFile.createNewFile();
//            if (created) {
//                LocalFileSystem.getInstance().refreshIoFiles(Collections.singletonList(newFile));
//                VirtualFile newVirtualFile = LocalFileSystem.getInstance().findFileByPath(newFile.getAbsolutePath());
//                if (newVirtualFile != null) {
//                    activeEnvFile = newVirtualFile;
//                    return true;
//                }
//            }
//            return false;
//        } catch (IOException e) {
//            LOG.error("Error creating blank env file", e);
//            return false;
//        }
//    }


//    /**
//     * Get the highest priority environment file available in the project
//     *
//     * @return The highest priority file, or null if none found
//     */
//    public VirtualFile getDefaultEnvFile() {
//        Map<EnvFileDefinition, List<VirtualFile>> allFiles = findAllEnvFiles();
//        if (allFiles.isEmpty()) {
//            return null;
//        }
//
//        // Sort definitions by priority and find the first one with files
//        List<EnvFileDefinition> sortedDefinitions = new ArrayList<>(allFiles.keySet());
//        sortedDefinitions.sort(Comparator.comparingInt(EnvFileDefinition::getPriority));
//
//        for (EnvFileDefinition definition : sortedDefinitions) {
//            List<VirtualFile> files = allFiles.get(definition);
//            if (files != null && !files.isEmpty()) {
//                return files.get(0);
//            }
//        }
//
//        return null;
//    }


}
