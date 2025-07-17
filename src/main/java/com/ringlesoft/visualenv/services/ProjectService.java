package com.ringlesoft.visualenv.services;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ringlesoft.visualenv.VisualEnvBundle;
import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.profile.ProfileManager;
import com.ringlesoft.visualenv.utils.CommandRunner;
import com.ringlesoft.visualenv.utils.ProjectDetector;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

/**
 * Project service for Visual Env plugin.
 */
@Service(Service.Level.PROJECT)
public final class ProjectService {
    public Project project;
    private static final Logger LOG = Logger.getInstance(ProjectService.class);
    private final Random random = new Random();
    private String projectType;
    private String activeEnvFile;


    public ProjectService(Project project) {
        LOG.info(VisualEnvBundle.message("projectService", project.getName()));
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getProjectType() {
        return projectType;
    }

    public void initialize() {
        projectType = ProjectDetector.getProjectType(project);
        System.out.println("Project type: " + projectType);
        EnvFileService envFileService = project.getService(EnvFileService.class);
        EnvProfile activeProfile = ProfileManager.getProfileByName(projectType);
        envFileService.setActiveProfile(activeProfile);
        scanAndProcessEnvFiles(project, activeProfile);
        showNotification("Hello there!", "This is Visual Env!", NotificationType.INFORMATION);
    }

    public String getActiveEnvFile() {
        return activeEnvFile;
    }

    public void setActiveEnvFile(String envFile) {
        activeEnvFile = envFile;
    }

    /**
     * Gets a random number between 1 and 100.
     *
     * @return a random number
     */
    public int getRandomNumber() {
        return random.nextInt(100) + 1;
    }

    /**
     * Checks for .env files in the project root and creates one from .env.example if needed.
     *
     * @param project The current project
     */
    private void scanAndProcessEnvFiles(@NotNull Project project, EnvProfile profile) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        List<EnvFileDefinition> envFileDefinitions = profile.getEnvFileDefinitions(); // Add null check>
        List<VirtualFile> foundFiles = new java.util.ArrayList<>();

        for (EnvFileDefinition envFileDefinition : envFileDefinitions) {
            VirtualFile envFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, envFileDefinition.getName()).toString());
            if (envFile != null) {
                // Parse existing .env file but do it safely
                EnvFileService envFileService = project.getService(EnvFileService.class);
                if (envFileService != null) {  // Add null check
                    envFileService.parseEnvFile(envFile);
                }
                if (envFileDefinition.isPrimary()) {
                    setActiveEnvFile(envFileDefinition.getName());
                }
                foundFiles.add(envFile);
            }
        }
    }

    public CommandRunner getCommandRunner() {
        return new CommandRunner(project);
    }

    /**
     * Display a notification
     * @param title Title of the notification
     * @param content Content of the notification
     * @param type Type of the notification
     * @param actions Actions to add to the notification
     */
    private void showNotification(String title, String content, NotificationType type, AnAction[] actions) {
        Notification notification = new Notification(
                "Visual Env Notification Group",
                title,
                content,
                type
        );
        for (AnAction action : actions) {
            notification.addAction(action);
        }
        Notifications.Bus.notify(notification, project);
    }

    public void showNotification(String title, String content, NotificationType type) {
        showNotification(title, content, type, new AnAction[0]);
    }
}