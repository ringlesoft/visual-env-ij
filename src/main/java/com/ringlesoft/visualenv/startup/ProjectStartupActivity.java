package com.ringlesoft.visualenv.startup;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.services.EnvVariableService;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Startup activity for Visual Env plugin.
 * This class is responsible for detecting .env files in the project root
 * and creating .env file from .env.example if it doesn't exist.
 */
public class ProjectStartupActivity implements ProjectActivity {
    private static final Logger LOG = Logger.getInstance(ProjectStartupActivity.class);
    private static final String ENV_FILE = ".env";
    private static final String ENV_EXAMPLE_FILE = ".env.example";

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        try {
            checkAndProcessEnvFiles(project);
        } catch (Exception e) {
            LOG.error("Error in ProjectStartupActivity", e);
        }
        return Unit.INSTANCE;
    }

    /**
     * Checks for .env files in the project root and creates one from .env.example if needed.
     *
     * @param project The current project
     */
    private void checkAndProcessEnvFiles(@NotNull Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        // Look for .env file
        VirtualFile envFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, ENV_FILE).toString());
        
        // If .env file doesn't exist, look for .env.example
        if (envFile == null) {
            VirtualFile envExampleFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, ENV_EXAMPLE_FILE).toString());
            
            if (envExampleFile != null && envExampleFile.exists()) {
                showCreateEnvFileNotification(project, envExampleFile);
            }
        } else {
            // Parse existing .env file
            EnvVariableService envVariableService = project.getService(EnvVariableService.class);
            envVariableService.parseEnvFile(envFile);
        }
    }

    /**
     * Shows a notification offering to create a .env file from .env.example.
     *
     * @param project The current project
     * @param envExampleFile The .env.example file
     */
    private void showCreateEnvFileNotification(@NotNull Project project, @NotNull VirtualFile envExampleFile) {
        Notification notification = new Notification(
                "VisualEnv",
                "Environment File Missing",
                "No .env file found, but .env.example exists. Would you like to create a .env file from the example?",
                NotificationType.INFORMATION
        );

        notification.addAction(new AnAction("Create .env File") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                try {
                    createEnvFromExample(project, envExampleFile);
                    notification.expire();
                } catch (IOException ex) {
                    LOG.error("Failed to create .env file", ex);
                    Notifications.Bus.notify(new Notification(
                            "VisualEnv",
                            "Error Creating .env File",
                            "Failed to create .env file: " + ex.getMessage(),
                            NotificationType.ERROR
                    ), project);
                }
            }
        });

        Notifications.Bus.notify(notification, project);
    }

    /**
     * Creates a .env file from .env.example.
     *
     * @param project The current project
     * @param envExampleFile The .env.example file
     * @throws IOException If there's an error creating the file
     */
    private void createEnvFromExample(@NotNull Project project, @NotNull VirtualFile envExampleFile) throws IOException {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }
        
        // Read the contents of .env.example
        String content = new String(envExampleFile.contentsToByteArray(), StandardCharsets.UTF_8);
        
        // Create .env file
        VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(basePath);
        if (baseDir != null) {
            VirtualFile envFile = baseDir.createChildData(this, ENV_FILE);
            envFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
            
            // Parse the newly created .env file
            EnvVariableService envVariableService = project.getService(EnvVariableService.class);
            envVariableService.parseEnvFile(envFile);
            
            // Show success notification
            Notifications.Bus.notify(new Notification(
                    "VisualEnv",
                    "Environment File Created",
                    ".env file has been created from .env.example",
                    NotificationType.INFORMATION
            ), project);
        }
    }
}
