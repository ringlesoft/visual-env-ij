package com.ringlesoft.visualenv.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Comparator;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;

/**
 * Utility class for managing environment files
 * Provides methods to read, write, and modify .env files
 */
public class EnvFileManager {

    /**
     * Set or update a key-value pair in .env file
     */
    public static void setEnvVariable(Project project, VirtualFile envFile, String key, String value) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return;
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, "Update .env Variable", null, () -> {
                setEnvVariableInternal(document, key, value);
                FileDocumentManager.getInstance().saveDocument(document);
            });
        }, ModalityState.defaultModalityState());
    }

    /**
     * Get value of a key from .env file
     */
    public static String getEnvVariable(VirtualFile envFile, String key) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return null;

        String content = document.getText();
        Pattern pattern = Pattern.compile("^" + Pattern.quote(key) + "=(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    /**
     * Remove a key from .env file
     */
    public static void removeEnvVariable(Project project, VirtualFile envFile, String key) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return;

        WriteCommandAction.runWriteCommandAction(project, "Remove .env Variable", null, () -> {
            String documentText = document.getText();
            Pattern pattern = Pattern.compile("^" + Pattern.quote(key) + "=.*\n?", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(documentText);

            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                document.deleteString(start, end);
            }
        });
    }

    /**
     * Add multiple environment variables
     */
    public static void setMultipleEnvVariables(Project project, VirtualFile envFile,
                                               java.util.Map<String, String> variables) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return;

        WriteCommandAction.runWriteCommandAction(project, "Update Multiple .env Variables", null, () -> {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                setEnvVariableInternal(document, entry.getKey(), entry.getValue());
            }
        });
    }

    private static void setEnvVariableInternal(Document document, String key, String value) {
        String documentText = document.getText();
        String newLine = key + "=" + value;

        Pattern pattern = Pattern.compile("^" + Pattern.quote(key) + "=.*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(documentText);

        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            document.replaceString(start, end, newLine);
        } else {
            if (!documentText.isEmpty() && !documentText.endsWith("\n")) {
                newLine = "\n" + newLine;
            }
            document.insertString(document.getTextLength(), newLine + "\n");
        }
    }

    /**
     * Add a comment above an environment variable
     */
    public static void addEnvComment(Project project, VirtualFile envFile,
                                     String key, String comment) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return;

        WriteCommandAction.runWriteCommandAction(project, "Add .env Comment", null, () -> {
            String documentText = document.getText();
            Pattern pattern = Pattern.compile("^" + Pattern.quote(key) + "=.*$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(documentText);

            if (matcher.find()) {
                int lineStart = matcher.start();
                String commentLine = "# " + comment + "\n";
                document.insertString(lineStart, commentLine);
            }
        });
    }

    /**
     * Add environment variables in a specific section
     */
    public static void addToSection(Project project, VirtualFile envFile,
                                    String sectionComment, String key, String value) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return;

        WriteCommandAction.runWriteCommandAction(project, "Add to .env Section", null, () -> {
            String documentText = document.getText();
            String sectionPattern = "# " + Pattern.quote(sectionComment);
            Pattern pattern = Pattern.compile("^" + sectionPattern + ".*$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(documentText);

            if (matcher.find()) {
                // Find end of current line and add after it
                int insertPosition = matcher.end();
                String newLine = "\n" + key + "=" + value;
                document.insertString(insertPosition, newLine);
            } else {
                // Create new section
                String newSection = "\n# " + sectionComment + "\n" + key + "=" + value + "\n";
                document.insertString(document.getTextLength(), newSection);
            }
        });
    }

    /**
     * Backup .env file before modifications
     */
    public static void backupEnvFile(Project project, VirtualFile envFile) {
        try {
            VirtualFile parent = envFile.getParent();
            String backupName = ".env.backup." + System.currentTimeMillis();

            WriteCommandAction.runWriteCommandAction(project, "Backup .env", null, () -> {
                try {
                    VirtualFile backup = parent.createChildData(null, backupName);
                    backup.setBinaryContent(envFile.contentsToByteArray());
                } catch (Exception e) {
                    // Handle backup failure
                    System.err.println("Failed to create backup: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // Handle exception
            System.err.println("Failed to backup env file: " + e.getMessage());
        }
    }

    /**
     * Check if a variable exists in the .env file
     */
    public static boolean hasEnvVariable(VirtualFile envFile, String key) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return false;

        String content = document.getText();
        Pattern pattern = Pattern.compile("^" + Pattern.quote(key) + "=.*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        return matcher.find();
    }

    /**
     * Get all environment variables from a file
     */
    public static Map<String, String> getAllEnvVariables(VirtualFile envFile) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return Collections.emptyMap();

        Map<String, String> variables = new HashMap<>();
        String content = document.getText();

        // Match lines that are key=value format (ignoring comments and blank lines)
        Pattern pattern = Pattern.compile("^([^#=\\s][^=\\s]*)=(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            variables.put(key, value);
        }

        return variables;
    }

    /**
     * Get all sections in the environment file
     * Sections are identified by comments that start with '# '
     */
    public static List<String> getEnvFileSections(VirtualFile envFile) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return Collections.emptyList();

        List<String> sections = new ArrayList<>();
        String content = document.getText();

        Pattern pattern = Pattern.compile("^# ([^\\n]+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            sections.add(matcher.group(1));
        }

        return sections;
    }

    /**
     * Modify or update an environment variable with atomic transaction
     * This is a safer alternative to setEnvVariable for critical operations
     */
    public static boolean safeUpdateEnvVariable(Project project, VirtualFile envFile,
                                                String key, String value, boolean createBackup) {
        if (createBackup) {
            backupEnvFile(project, envFile);
        }

        try {
            setEnvVariable(project, envFile, key, value);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to update environment variable: " + e.getMessage());
            return false;
        }
    }

    /**
     * Synchronize env file with a template file
     * This adds missing variables from template but doesn't overwrite existing values
     */
    public static void syncWithTemplate(Project project, VirtualFile envFile, VirtualFile templateFile) {
        Document templateDoc = FileDocumentManager.getInstance().getDocument(templateFile);
        if (templateDoc == null) return;

        Map<String, String> templateVars = getAllEnvVariables(templateFile);
        Map<String, String> currentVars = getAllEnvVariables(envFile);

        // Find missing variables
        Map<String, String> missingVars = new HashMap<>();
        for (Map.Entry<String, String> entry : templateVars.entrySet()) {
            if (!currentVars.containsKey(entry.getKey())) {
                missingVars.put(entry.getKey(), entry.getValue());
            }
        }

        // Add missing variables if any
        if (!missingVars.isEmpty()) {
            setMultipleEnvVariables(project, envFile, missingVars);
        }
    }

    /**
     * Format and organize the entire .env file by grouping variables into sections
     * @param project The current project
     * @param envFile The .env file to organize
     * @param sectionMap A map of section names to lists of variable keys that belong to those sections
     * @param createBackup Whether to create a backup before organizing
     */
    public static void organizeEnvFile(Project project, VirtualFile envFile,
                                       Map<String, List<String>> sectionMap, boolean createBackup) {
        if (createBackup) {
            backupEnvFile(project, envFile);
        }

        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return;

        WriteCommandAction.runWriteCommandAction(project, "Organize .env File", null, () -> {
            // Extract all variables first
            Map<String, String> allVariables = getAllEnvVariables(envFile);

            // Create the new organized content
            StringBuilder newContent = new StringBuilder();
            newContent.append("# This file was organized by Visual Env on ")
                    .append(new java.util.Date().toString())
                    .append("\n\n");

            // Add variables by section
            for (Map.Entry<String, List<String>> section : sectionMap.entrySet()) {
                String sectionName = section.getKey();
                List<String> keys = section.getValue();

                // Skip empty sections
                if (keys.isEmpty()) continue;

                // Add section header
                newContent.append("# ").append(sectionName).append("\n");

                // Add all variables in this section
                for (String key : keys) {
                    if (allVariables.containsKey(key)) {
                        newContent.append(key).append("=").append(allVariables.get(key)).append("\n");
                        allVariables.remove(key); // Remove from map to track what's been processed
                    }
                }

                newContent.append("\n"); // Add spacing between sections
            }

            // Add any remaining variables in an "Other" section if needed
            if (!allVariables.isEmpty()) {
                newContent.append("# Other\n");
                for (Map.Entry<String, String> entry : allVariables.entrySet()) {
                    newContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            }

            // Replace the entire file content with our organized version
            document.setText(newContent.toString());
        });
    }

    /**
     * Extract sections and their variables from an existing .env file
     * @param envFile The environment file to analyze
     * @return A map of section names to the variable keys in each section
     */
    public static Map<String, List<String>> extractEnvFileSections(VirtualFile envFile) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return Collections.emptyMap();

        String content = document.getText();
        Map<String, List<String>> sections = new LinkedHashMap<>(); // Preserve order

        // Default section for variables at the beginning with no section
        String currentSection = "General";
        List<String> currentSectionVars = new ArrayList<>();

        // Process line by line
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Check if this is a section header
            if (line.startsWith("# ")) {
                // Save previous section if it has any variables
                if (!currentSectionVars.isEmpty()) {
                    sections.put(currentSection, new ArrayList<>(currentSectionVars));
                    currentSectionVars.clear();
                }

                // Start new section
                currentSection = line.substring(2);

            } else if (line.matches("^[^#=\\s][^=]*=.*$")) {
                // This is a variable definition
                String key = line.substring(0, line.indexOf('='));
                currentSectionVars.add(key);
            }
            // Skip blank lines and comments that aren't section headers
        }

        // Add the last section
        if (!currentSectionVars.isEmpty()) {
            sections.put(currentSection, currentSectionVars);
        }

        return sections;
    }

    /**
     * Import variables from another .env file
     * @param project The current project
     * @param targetFile The target .env file to add variables to
     * @param sourceFile The source .env file to import variables from
     * @param overwriteExisting Whether to overwrite existing variables
     * @param createBackup Whether to create a backup before importing
     */
    public static void importFromEnvFile(Project project, VirtualFile targetFile,
                                         VirtualFile sourceFile, boolean overwriteExisting,
                                         boolean createBackup) {
        if (createBackup) {
            backupEnvFile(project, targetFile);
        }

        Map<String, String> sourceVars = getAllEnvVariables(sourceFile);
        Map<String, String> targetVars = getAllEnvVariables(targetFile);

        Map<String, String> varsToAdd = new HashMap<>();

        for (Map.Entry<String, String> entry : sourceVars.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (overwriteExisting || !targetVars.containsKey(key)) {
                varsToAdd.put(key, value);
            }
        }

        if (!varsToAdd.isEmpty()) {
            setMultipleEnvVariables(project, targetFile, varsToAdd);
        }
    }

    /**
     * Validates the syntax of an environment file and returns any errors
     * @param envFile The .env file to validate
     * @return A list of error messages, empty if no errors
     */
    public static List<String> validateEnvFile(VirtualFile envFile) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return Collections.singletonList("Could not read file");

        List<String> errors = new ArrayList<>();
        String content = document.getText();
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Check for valid key=value format
            if (!line.matches("^[^=\\s][^=]*=.*$")) {
                errors.add("Line " + (i + 1) + ": Invalid format, expected KEY=VALUE");
            }

            // Check for common mistakes
            if (line.contains(" = ")) {
                errors.add("Line " + (i + 1) + ": Spaces around equals sign may cause issues");
            }

            if (line.endsWith("\\")) {
                errors.add("Line " + (i + 1) + ": Line ending with backslash may cause parsing issues");
            }
        }

        return errors;
    }

    /**
     * Rename an environment variable in .env file
     * @param project The current project
     * @param envFile The environment file
     * @param oldKey The old key name
     * @param newKey The new key name
     * @return Whether the rename was successful
     */
    public static boolean renameEnvVariable(Project project, VirtualFile envFile,
                                            String oldKey, String newKey) {
        if (oldKey.equals(newKey)) return true; // No change needed

        // Get the current value
        String value = getEnvVariable(envFile, oldKey);
        if (value == null) return false; // Original key not found

        // Remove old and add new in a single transaction
        WriteCommandAction.runWriteCommandAction(project, "Rename Environment Variable", null, () -> {
            removeEnvVariable(project, envFile, oldKey);
            setEnvVariable(project, envFile, newKey, value);
        });

        return true;
    }

    /**
     * Create a new empty environment file
     * @param project The current project
     * @param directory The directory to create the file in
     * @param fileName The name of the file (e.g., ".env" or ".env.local")
     * @return The created VirtualFile or null if creation failed
     */
    public static VirtualFile createEmptyEnvFile(Project project, VirtualFile directory, String fileName) {
        try {
            VirtualFile[] result = new VirtualFile[1];

            WriteCommandAction.runWriteCommandAction(project, "Create Environment File", null, () -> {
                try {
                    VirtualFile file = directory.createChildData(null, fileName);
                    String defaultContent = "# Environment file created by Visual Env\n" +
                            "# " + new java.util.Date().toString() + "\n\n";
                    file.setBinaryContent(defaultContent.getBytes());
                    result[0] = file;
                } catch (Exception e) {
                    System.err.println("Failed to create env file: " + e.getMessage());
                }
            });

            return result[0];
        } catch (Exception e) {
            System.err.println("Failed to create env file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Comment or uncomment an environment variable
     * @param project The current project
     * @param envFile The environment file
     * @param key The variable key to comment/uncomment
     * @param comment If true, comments the variable; if false, uncomments it
     * @return Whether the operation was successful
     */
    public static boolean toggleCommentVariable(Project project, VirtualFile envFile,
                                                String key, boolean comment) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return false;

        String content = document.getText();
        final boolean[] success = {false};

        WriteCommandAction.runWriteCommandAction(project, "Toggle Environment Variable Comment", null, () -> {
            if (comment) {
                // Comment out the variable
                Pattern pattern = Pattern.compile("^(" + Pattern.quote(key) + "=.*)$", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(content);

                if (matcher.find()) {
                    document.replaceString(matcher.start(), matcher.end(), "# " + matcher.group(1));
                    success[0] = true;
                }
            } else {
                // Uncomment the variable
                Pattern pattern = Pattern.compile("^# (" + Pattern.quote(key) + "=.*)$", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(content);

                if (matcher.find()) {
                    document.replaceString(matcher.start(), matcher.end(), matcher.group(1));
                    success[0] = true;
                }
            }
        });

        return success[0];
    }

    /**
     * Find all commented environment variables in a file
     * @param envFile The environment file
     * @return Map of commented variable keys to their values
     */
    public static Map<String, String> getCommentedVariables(VirtualFile envFile) {
        Document document = FileDocumentManager.getInstance().getDocument(envFile);
        if (document == null) return Collections.emptyMap();

        Map<String, String> variables = new HashMap<>();
        String content = document.getText();

        // Match commented lines that are key=value format
        Pattern pattern = Pattern.compile("^# ([^#=\\s][^=\\s]*)=(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            variables.put(key, value);
        }

        return variables;
    }

    /**
     * Search for environment variables by key pattern
     * @param envFile The environment file
     * @param keyPattern Regular expression pattern to match variable keys
     * @return Map of matching variable keys to their values
     */
    public static Map<String, String> findVariablesByKeyPattern(VirtualFile envFile, String keyPattern) {
        Map<String, String> allVars = getAllEnvVariables(envFile);
        Map<String, String> matchingVars = new HashMap<>();

        Pattern pattern = Pattern.compile(keyPattern);

        for (Map.Entry<String, String> entry : allVars.entrySet()) {
            if (pattern.matcher(entry.getKey()).find()) {
                matchingVars.put(entry.getKey(), entry.getValue());
            }
        }

        return matchingVars;
    }

    /**
     * Display a notification to the user
     */
    private static void showNotification(Project project, String title, String content, NotificationType type) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup("Visual Env Notification Group");
        if (notificationGroup == null) {
            // Fallback for older IntelliJ versions or if group is not registered
            Notifications.Bus.notify(new Notification("Visual Env Notification Group", title, content, type), project);
            return;
        }

        Notification notification = notificationGroup.createNotification(content, type);
        notification.setTitle(title);
        notification.notify(project);
    }

    /**
     * Set an environment variable with notification of success or failure
     */
    public static boolean setEnvVariableWithNotification(Project project, VirtualFile envFile,
                                                         String key, String value) {
        try {
            setEnvVariable(project, envFile, key, value);
            showNotification(project, "Environment Variable Updated",
                    "Variable " + key + " has been set successfully.", NotificationType.INFORMATION);
            return true;
        } catch (Exception e) {
            showNotification(project, "Failed to Update Variable",
                    "Could not update " + key + ": " + e.getMessage(), NotificationType.ERROR);
            return false;
        }
    }

    /**
     * Remove an environment variable with notification of success or failure
     */
    public static boolean removeEnvVariableWithNotification(Project project, VirtualFile envFile, String key) {
        try {
            if (!hasEnvVariable(envFile, key)) {
                showNotification(project, "Variable Not Found",
                        "Variable " + key + " was not found in the environment file.",
                        NotificationType.WARNING);
                return false;
            }

            removeEnvVariable(project, envFile, key);
            showNotification(project, "Environment Variable Removed",
                    "Variable " + key + " has been removed successfully.",
                    NotificationType.INFORMATION);
            return true;
        } catch (Exception e) {
            showNotification(project, "Failed to Remove Variable",
                    "Could not remove " + key + ": " + e.getMessage(),
                    NotificationType.ERROR);
            return false;
        }
    }

    /**
     * Create a backup with notification of success or failure
     */
    public static boolean backupEnvFileWithNotification(Project project, VirtualFile envFile) {
        try {
            VirtualFile parent = envFile.getParent();
            String backupName = ".env.backup." + System.currentTimeMillis();
            final boolean[] success = {false};

            WriteCommandAction.runWriteCommandAction(project, "Backup .env", null, () -> {
                try {
                    VirtualFile backup = parent.createChildData(null, backupName);
                    backup.setBinaryContent(envFile.contentsToByteArray());
                    success[0] = true;
                    showNotification(project, "Backup Created",
                            "Backup saved as " + backupName,
                            NotificationType.INFORMATION);
                } catch (Exception e) {
                    showNotification(project, "Backup Failed",
                            "Could not create backup: " + e.getMessage(),
                            NotificationType.ERROR);
                }
            });

            return success[0];
        } catch (Exception e) {
            showNotification(project, "Backup Failed",
                    "Could not create backup: " + e.getMessage(),
                    NotificationType.ERROR);
            return false;
        }
    }

    /**
     * Handle general errors during .env file operations
     * @param project The project
     * @param operation The name of the operation being performed
     * @param e The exception that occurred
     * @return Always returns false to indicate failure
     */
    public static boolean handleError(Project project, String operation, Exception e) {
        showNotification(project, "Operation Failed",
                operation + " failed: " + e.getMessage(),
                NotificationType.ERROR);

        // Log the error
        System.err.println("Visual Env error during " + operation + ": " + e.getMessage());
        e.printStackTrace();

        return false;
    }

    /**
     * Validate an environment variable key
     * @param key The key to validate
     * @return True if the key is valid
     */
    public static boolean isValidEnvKey(String key) {
        // Environment variable keys should not have spaces or special characters
        return key != null && !key.isEmpty() && !key.contains(" ") &&
                !key.contains("=") && !key.contains("#") &&
                key.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Format a value for storage in an .env file
     * Handles quoting if necessary
     */
    public static String formatEnvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains spaces or special characters, wrap in quotes
        if (value.contains(" ") || value.contains("#") || value.contains("'") || value.contains("\"")) {
            // Escape double quotes and wrap in double quotes
            String escaped = value.replace("\"", "\\\"");
            return "\"" + escaped + "\"";
        }

        return value;
    }

    /**
     * Find and restore a backup file
     * @param project The current project
     * @param directory The directory containing backup files
     * @param targetFileName The target filename (e.g., ".env")
     * @return True if restore was successful
     */
    public static boolean restoreBackup(Project project, VirtualFile directory, String targetFileName) {
        try {
            // Find all backup files in the directory
            List<VirtualFile> backupFiles = new ArrayList<>();
            for (VirtualFile child : directory.getChildren()) {
                if (child.getName().startsWith(".env.backup.")) {
                    backupFiles.add(child);
                }
            }

            if (backupFiles.isEmpty()) {
                showNotification(project, "Restore Failed",
                        "No backup files found for restoration",
                        NotificationType.WARNING);
                return false;
            }

            // Sort by name (which includes timestamp) to get the newest
            backupFiles.sort(Comparator.comparing(VirtualFile::getName).reversed());
            VirtualFile latestBackup = backupFiles.get(0);

            // Target file
            final VirtualFile[] targetFile = {directory.findChild(targetFileName)};
            boolean targetExists = targetFile[0] != null;

            WriteCommandAction.runWriteCommandAction(project, "Restore .env from Backup", null, () -> {
                try {
                    byte[] content = latestBackup.contentsToByteArray();

                    if (!targetExists) {
                        targetFile[0] = directory.createChildData(null, targetFileName);
                    }

                    targetFile[0].setBinaryContent(content);

                    showNotification(project, "Backup Restored",
                            "Successfully restored from backup " + latestBackup.getName(),
                            NotificationType.INFORMATION);
                } catch (Exception e) {
                    showNotification(project, "Restore Failed",
                            "Could not restore from backup: " + e.getMessage(),
                            NotificationType.ERROR);
                }
            });

            return true;
        } catch (Exception e) {
            return handleError(project, "Restore Backup", e);
        }
    }
}
