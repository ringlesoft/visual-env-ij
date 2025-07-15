package com.ringlesoft.visualenv.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.ringlesoft.visualenv.services.EnvVariableService;
import com.ringlesoft.visualenv.toolWindow.EnvEditorTab;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for file save events and updates the UI when an environment file is saved.
 * This ensures that the UI always displays the current state of the file, even when
 * it's modified outside our plugin (e.g., by external tools or other editors).
 */
public class FileSaveListener {
    private static final Logger LOG = Logger.getInstance(FileSaveListener.class);
    private final Project project;
    private final EnvVariableService envVariableService;
    private EnvEditorTab envEditorTab;
    private MessageBusConnection connection;

    public FileSaveListener(Project project, EnvVariableService envVariableService) {
        this.project = project;
        this.envVariableService = envVariableService;
    }

    /**
     * Set the EnvEditorTab that should be updated when files are saved
     * 
     * @param envEditorTab The editor tab to update
     */
    public void setEnvEditorTab(EnvEditorTab envEditorTab) {
        this.envEditorTab = envEditorTab;
    }

    /**
     * Set up the listener for file save events
     */
    public void setupListener() {
        // Connect to the message bus
        connection = project.getMessageBus().connect();
        
        // Subscribe to document save events
        connection.subscribe(FileDocumentManagerListener.TOPIC, 
            new FileDocumentManagerListener() {
                @Override
                public void beforeDocumentSaving(@NotNull Document document) {
                    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                    if (file != null && isFileWeCareAbout(file)) {
                        LOG.debug("Environment file is being saved: " + file.getPath());
                        // Update your UI here
                        updateUI(file);
                    }
                }
            });
        
        LOG.info("FileSaveListener setup completed for project: " + project.getName());
    }

    /**
     * Check if this is a file we need to track (an environment file)
     * 
     * @param file The file to check
     * @return true if this is an environment file we should track
     */
    private boolean isFileWeCareAbout(VirtualFile file) {
        // Check if this is an environment file we're tracking
        if (envVariableService == null) {
            return false;
        }
        
        // Check if it's one of our environment files
        VirtualFile activeFile = envVariableService.getActiveEnvFile();
        if (file.equals(activeFile)) {
            return true;
        }
        
        // Check other files managed by the env variable service
        return envVariableService.isEditableEnvFile(file);
    }

    /**
     * Get the currently selected file in the env editor
     * 
     * @return The currently selected file or null if none
     */
    private VirtualFile getCurrentlySelectedFile() {
        if (envEditorTab != null) {
            String selectedFilePath = envEditorTab.getSelectedFilePath();
            if (selectedFilePath != null) {
                return envVariableService.findFileByPath(selectedFilePath);
            }
        }
        return null;
    }

    /**
     * Update the UI when a file is saved
     * 
     * @param savedFile The file that was saved
     */
    private void updateUI(VirtualFile savedFile) {
        if (envEditorTab == null) {
            LOG.warn("Cannot update UI - envEditorTab is not set");
            return;
        }
        // Ensure UI updates happen on the EDT
        ApplicationManager.getApplication().invokeLater(() -> {
            LOG.debug("Updating UI after file save: " + savedFile.getPath());
            // If this is the currently selected file, reload it
            String currentPath = envEditorTab.getSelectedFilePath();
            if (currentPath != null && savedFile.getPath().equals(currentPath)) {
                envEditorTab.reloadCurrentEnvFile();
            } else {
                // Otherwise, just reload the file in the service's cache
                envVariableService.parseEnvFile(savedFile);
            }
        });
    }

    /**
     * Clean up resources when the listener is no longer needed
     */
    public void dispose() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        LOG.info("FileSaveListener disposed for project: " + project.getName());
    }
}
