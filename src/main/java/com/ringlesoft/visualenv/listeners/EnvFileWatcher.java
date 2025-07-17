package com.ringlesoft.visualenv.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import com.ringlesoft.visualenv.services.EnvFileService;
import com.ringlesoft.visualenv.toolWindow.VisualEnvToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class EnvFileWatcher {
    private final Project project;
    private MessageBusConnection connection;
    private VisualEnvToolWindowFactory toolWindowFactory;

    public EnvFileWatcher(Project project, VisualEnvToolWindowFactory toolWindowFactory) {
        this.project = project;
        this.toolWindowFactory = toolWindowFactory;
    }

    public void startWatching() {
        connection = project.getMessageBus().connect();

        AsyncFileListener listener = new AsyncFileListener() {
            @Override
            public @Nullable ChangeApplier prepareChange(@NotNull List<? extends @NotNull VFileEvent> events) {
                List<VFileEvent> relevantEvents = new ArrayList<>();

                for (VFileEvent event : events) {
                    if (isRelevantEvent(event)) {
                        relevantEvents.add(event);
                    }
                }

                if (relevantEvents.isEmpty()) {
                    return null;
                }

                return new ChangeApplier() {
                    @Override
                    public void afterVfsChange() {
                        // This runs after the VFS change is applied
                        for (VFileEvent event : relevantEvents) {
                            handleEvent(event);
                        }
                    }
                };
            }
        };

        VirtualFileManager.getInstance().addAsyncFileListener(listener, connection);
    }

    private boolean isRelevantEvent(VFileEvent event) {
        if (event instanceof VFileCreateEvent createEvent) {
            String fileName = createEvent.getChildName();
            return fileName.startsWith(".env") && isInProject(createEvent);
        } else if (event instanceof VFileDeleteEvent deleteEvent) {
            VirtualFile file = deleteEvent.getFile();
            return file.getName().startsWith(".env") && isInProject(file);
        }
        return false;
    }

    private boolean isInProject(VFileCreateEvent event) {
        VirtualFile parent = event.getParent();
        String projectPath = project.getBasePath();
        return projectPath != null && parent.getPath().startsWith(projectPath);
    }

    private boolean isInProject(VirtualFile file) {
        String projectPath = project.getBasePath();
        return projectPath != null && file.getPath().startsWith(projectPath);
    }

    private void handleEvent(VFileEvent event) {
        if (event instanceof VFileCreateEvent createEvent) {
            handleFileCreated(createEvent);
        } else if (event instanceof VFileDeleteEvent deleteEvent) {
            handleFileDeleted(deleteEvent.getFile());
        }
    }

    private void handleFileCreated(VFileCreateEvent event) {
        String fileName = event.getChildName();
        System.out.println("New .env file created: " + fileName);

        // Update your UI on the EDT
        SwingUtilities.invokeLater(() -> {
            EnvFileService envFileService = project.getService(EnvFileService.class);
            envFileService.rescanEnvFiles();
            toolWindowFactory.updateUI();
        });
    }

    private void handleFileDeleted(VirtualFile file) {
        System.out.println(".env file deleted: " + file.getName());

        SwingUtilities.invokeLater(() -> {
            EnvFileService envFileService = project.getService(EnvFileService.class);
            envFileService.rescanEnvFiles();
            toolWindowFactory.updateUI();
        });
    }

    public void stopWatching() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}