package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.actionSystem.ActionStub;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.services.EnvVariableService;
import com.ringlesoft.visualenv.services.ProjectService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class VisualEnvToolWindowFactory implements ToolWindowFactory {

    private Project project;
    private EnvVariableService envService;
    private ProjectService projectService;
    private JTextField filterField;
    private JComboBox<String> envFileSelector;
    private JComboBox<String> profileSelector;
    private JLabel projectTypeLabel;
    private Map<String, EnvProfile> availableProfiles;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JPanel controlPanel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        this.envService = project.getService(EnvVariableService.class);
        this.projectService = project.getService(ProjectService.class);

        mainPanel = new JPanel(new BorderLayout());
        controlPanel = createControlPanel();
        
        contentPanel = new JPanel(new BorderLayout());
        tabbedPane = new JBTabbedPane();
        
        // Create Environment Variables tab
        JPanel envPanel = new EnvEditorTab(project, envService, projectService);
        tabbedPane.addTab("Environment Variables", envPanel);
        
        // Add Artisan tab if supported
        if (envService.getActiveProfile().supportsArtisanCommands()) {
            JPanel artisanPanel = createCliActionsPanel();
            tabbedPane.addTab("CLI Commands", artisanPanel);
        }
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * Create the control panel with profile selector and project type
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5));
        
        JPanel topPanel = new JPanel(new GridLayout(1, 1, 0, 5));
        
        // First row - project type and profile selector
        JPanel projectTypePanel = new JPanel(new BorderLayout());

        // Add project type detection label
        String profileName = envService.getActiveProfile().getProfileName();
        projectTypeLabel = new JBLabel("Profile: " + profileName);
        projectTypeLabel.setBorder(JBUI.Borders.emptyRight(10));
        projectTypePanel.add(projectTypeLabel, BorderLayout.WEST);
        
        topPanel.add(projectTypePanel);
        panel.add(topPanel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Create the CLI commands panel for the active profile
     */
    private JPanel createCliActionsPanel() {
        return new CliActionsTab(project, envService);
    }

    /**
     * Update the UI when the profile changes
     */
    private void updateUI() {
        // Update project type label
        String projectType = projectService.getProjectType();
        projectTypeLabel.setText("Project type: " + projectType);
        
        // Update button visibility based on profile
        Container buttonPanel = controlPanel;
        
        // Find action buttons in the control panel (assuming they're in a box layout or similar)
        for (Component component : buttonPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().equals("Create from Example")) {
                    // Only show if profile supports template files
                    button.setVisible(envService.getActiveProfile().supportsTemplateFiles());
                }
            }
        }
        
        // Update tabs
        while (tabbedPane.getTabCount() > 0) {
            tabbedPane.remove(0);
        }
        
        // Create Environment Variables tab
        JPanel envPanel = new EnvEditorTab(project, envService, projectService);
        tabbedPane.addTab("Environment Variables", envPanel);
        
        // Add CLI Commands tab if supported
        // This tab is currently Disabled
//        if (envService.getActiveProfile().supportsArtisanCommands()) {
//            JPanel artisanPanel = createCliActionsPanel();
//            tabbedPane.addTab("CLI Commands", artisanPanel);
//        }

        // Refresh UI
        mainPanel.revalidate();
        mainPanel.repaint();
    }


    /**
     * Add a new environment variable button and functionality
     */
    public void addAddVariableButton() {
        JButton addButton = new JButton("+ Add Variable");
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(e -> {
            // Create a new panel with text fields for key and value
            JPanel newVarPanel = new JPanel(new BorderLayout(10, 0));
            newVarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            newVarPanel.setBorder(JBUI.Borders.emptyBottom(5));

            JTextField keyField = new JTextField("NEW_VARIABLE");
            keyField.setBorder(JBUI.Borders.emptyRight(10));
            JTextField valueField = new JTextField("");

            newVarPanel.add(keyField, BorderLayout.WEST);
            newVarPanel.add(valueField, BorderLayout.CENTER);

            // Insert it before the add button
            contentPanel.add(newVarPanel, contentPanel.getComponentCount() - 1);

            // Focus the key field
            keyField.requestFocus();
            keyField.selectAll();
            // Refresh UI
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        contentPanel.add(addButton);
    }


    
    private JPanel getContentPanel() {
        return contentPanel;
    }
    
    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // Always make the tool window available
        return true;
    }
}
