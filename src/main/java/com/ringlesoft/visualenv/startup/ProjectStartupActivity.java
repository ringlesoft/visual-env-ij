package com.ringlesoft.visualenv.startup;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.services.EnvFileService;
import com.ringlesoft.visualenv.services.ProjectService;
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
        // Perform initialization in a later invocation when components are loaded
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                LOG.info("Initializing Visual Env for project: " + project.getName());
                initializeProject(project);
            } catch (Exception e) {
                LOG.error("Error in Visual Env initialization", e);
            }
        });
        
        return Unit.INSTANCE;
    }

    private void initializeProject(@NotNull Project project) {
        ProjectService projectService = project.getService(ProjectService.class);
        if (projectService != null) {  // Add null check to be extra safe
            projectService.initialize();
        }
    }

}
