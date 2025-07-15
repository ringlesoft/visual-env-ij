package com.ringlesoft.visualenv.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.ringlesoft.visualenv.utils.ProjectDetector;
import com.ringlesoft.visualenv.VisualEnvBundle;

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
}
