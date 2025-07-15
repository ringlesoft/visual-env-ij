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
    private static final Logger LOG = Logger.getInstance(ProjectService.class);
    private final Random random = new Random();
    public String projectType;
    public Project project;

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

    /**
     * Gets a random number between 1 and 100.
     *
     * @return a random number
     */
    public int getRandomNumber() {
        return random.nextInt(100) + 1;
    }
}
