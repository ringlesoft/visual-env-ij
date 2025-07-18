package com.ringlesoft.visualenv.services;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.ringlesoft.visualenv.VisualEnvBundle;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.profile.ProfileManager;
import com.ringlesoft.visualenv.utils.CommandRunner;
import com.ringlesoft.visualenv.utils.ProjectDetector;

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
        this.project = project;
        LOG.info(VisualEnvBundle.message("projectService", project.getName()));
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getProjectType() {
        return projectType;
    }

    public void initialize() {
        if(project == null) {
            LOG.info("Project is null");
            return;
        }
        projectType = ProjectDetector.getProjectType(project);
        System.out.println("Project type: " + projectType);
        EnvFileService envFileService = project.getService(EnvFileService.class);
        EnvProfile activeProfile = ProfileManager.getProfileByName(projectType);
        envFileService.setActiveProfile(activeProfile);
        envFileService.scanAndProcessEnvFiles();
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