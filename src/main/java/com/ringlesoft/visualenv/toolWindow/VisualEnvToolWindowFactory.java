package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.profile.ProfileManager;
import com.ringlesoft.visualenv.services.EnvVariableService;
import com.ringlesoft.visualenv.utils.ProjectDetector;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class VisualEnvToolWindowFactory implements ToolWindowFactory {

    private Project project;
    private EnvVariableService envService;
    private final Map<String, EnvGroupPanel> groupPanels = new HashMap<>();
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

        mainPanel = new JPanel(new BorderLayout());
        controlPanel = createControlPanel();
        
        contentPanel = new JPanel(new BorderLayout());
        tabbedPane = new JBTabbedPane();
        
        // Create Environment Variables tab
        JPanel envPanel = createEnvPanel();
        tabbedPane.addTab("Environment Variables", envPanel);
        
        // Only add Artisan commands tab for Laravel projects
        EnvProfile activeProfile = envService.getActiveProfile();
        if (activeProfile.supportsArtisanCommands()) {
            JPanel artisanPanel = createCliActionsPanel();
            tabbedPane.addTab("Cli Commands", artisanPanel);
        }
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        // Look for .env files in the project and load the first one found
        loadEnvFiles();
    }

    /**
     * Create the control panel with file selector and filter
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        
        // First row - project type and profile selector
        JPanel projectTypePanel = new JPanel(new BorderLayout());
        
        // Add project type detection label
        String projectType = ProjectDetector.getProjectType(project);
        projectTypeLabel = new JBLabel("Project type: " + projectType);
        projectTypeLabel.setBorder(JBUI.Borders.emptyRight(10));
        projectTypePanel.add(projectTypeLabel, BorderLayout.WEST);
        
        // Add profile selector
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.add(new JBLabel("Profile:"), BorderLayout.WEST);
        
        profileSelector = new JComboBox<>();
        
        // Populate profiles
        availableProfiles = new HashMap<>();
        for (EnvProfile profile : ProfileManager.getAllProfiles()) {
            profileSelector.addItem(profile.getProfileName());
            availableProfiles.put(profile.getProfileName(), profile);
        }
        
        // Set the current profile
        profileSelector.setSelectedItem(envService.getActiveProfile().getProfileName());
        
        // Add listener to change profile
        profileSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String profileName = (String) profileSelector.getSelectedItem();
                EnvProfile selectedProfile = availableProfiles.get(profileName);
                if (selectedProfile != null) {
                    envService.setActiveProfile(selectedProfile);
                    updateUI();
                }
            }
        });
        
        profilePanel.add(profileSelector, BorderLayout.CENTER);
        projectTypePanel.add(profilePanel, BorderLayout.EAST);
        
        topPanel.add(projectTypePanel);
        
        // Second row - file selector and filter
        JPanel fileSelectorPanel = new JPanel(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JBLabel("Env File:"), BorderLayout.WEST);
        
        envFileSelector = new JComboBox<>();
        envFileSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) envFileSelector.getSelectedItem();
                if (selected != null) {
                    loadEnvFile(selected);
                }
            }
        });
        leftPanel.add(envFileSelector, BorderLayout.CENTER);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JBLabel("Filter:"), BorderLayout.WEST);
        filterField = new JBTextField();
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterVariables();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterVariables();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterVariables();
            }
        });
        rightPanel.add(filterField, BorderLayout.CENTER);
        
        fileSelectorPanel.add(leftPanel, BorderLayout.CENTER);
        fileSelectorPanel.add(rightPanel, BorderLayout.EAST);
        
        topPanel.add(fileSelectorPanel);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Bottom row - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Button to create .env from .env.example (only for Laravel)
        JButton createFromExampleButton = new JButton("Create from Example");
        createFromExampleButton.addActionListener(e -> {
            String basePath = project.getBasePath();
            if (basePath != null) {
                VirtualFile exampleFile = LocalFileSystem.getInstance()
                        .refreshAndFindFileByPath(basePath + "/.env.example");
                if (exampleFile != null) {
                    boolean success = envService.createEnvFromExample(exampleFile);
                    if (success) {
                        loadEnvFiles(); // Refresh file list
                    }
                }
            }
        });
        
        // Only show create from example button for profiles that support it
        if (envService.getActiveProfile().getProfileName().equals("Laravel")) {
            buttonPanel.add(createFromExampleButton);
        }
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Create the main environment variables panel
     */
    private JPanel createEnvPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5));

        JPanel envVarsPanel = new JPanel();
        envVarsPanel.setLayout(new BoxLayout(envVarsPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JBScrollPane(envVarsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the Artisan commands panel for Laravel projects
     */
    private JPanel createCliActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5));

        JPanel commandsPanel = new JPanel();
        commandsPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5);
        
        // Add common Artisan commands as buttons
        String[] commands = {
                "key:generate", 
                "migrate", 
                "cache:clear", 
                "route:list", 
                "make:controller", 
                "make:model"
        };
        
        for (String command : commands) {
            JButton button = new JButton(command);
            button.addActionListener(e -> {
                // Execute the Artisan command
                String result;
                
                // For make:controller and make:model, we need to prompt for a name
                if (command.equals("make:controller") || command.equals("make:model")) {
                    String name = JOptionPane.showInputDialog(
                            panel,
                            "Enter the name for the " + 
                                    (command.equals("make:controller") ? "controller" : "model") + ":",
                            "Create " + (command.equals("make:controller") ? "Controller" : "Model"),
                            JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (name == null || name.trim().isEmpty()) {
                        return;
                    }
                    
                    result = envService.executeArtisanCommand(command + " " + name);
                } else {
                    result = envService.executeArtisanCommand(command);
                }
                
                // Show the result in a dialog
                JTextArea textArea = new JTextArea(result);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JBScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                
                JOptionPane.showMessageDialog(
                        panel,
                        scrollPane,
                        "Artisan Command Result",
                        JOptionPane.INFORMATION_MESSAGE
                );
            });
            
            commandsPanel.add(button, gbc);
            gbc.gridx++;
            
            if (gbc.gridx > 2) {
                gbc.gridx = 0;
                gbc.gridy++;
            }
        }
        
        // Add custom command execution
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        commandsPanel.add(new JBLabel("Custom Command:"), gbc);
        
        gbc.gridy++;
        JTextField commandField = new JBTextField(20);
        commandsPanel.add(commandField, gbc);
        
        gbc.gridy++;
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(e -> {
            String command = commandField.getText().trim();
            if (!command.isEmpty()) {
                String result = envService.executeArtisanCommand(command);
                
                JTextArea textArea = new JTextArea(result);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JBScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                
                JOptionPane.showMessageDialog(
                        panel,
                        scrollPane,
                        "Artisan Command Result",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        commandsPanel.add(executeButton, gbc);
        
        panel.add(new JBScrollPane(commandsPanel), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Load all available .env files in the project
     */
    private void loadEnvFiles() {
        String basePath = project.getBasePath();
        if (basePath == null) return;
        
        File baseDir = new File(basePath);
        if (!baseDir.exists() || !baseDir.isDirectory()) return;
        
        envFileSelector.removeAllItems();
        
        // Get common env filenames from the active profile
        String[] commonEnvFiles = envService.getActiveProfile().getCommonEnvFiles();
        
        // Look for these files in the project root
        for (String fileName : commonEnvFiles) {
            File envFile = new File(baseDir, fileName);
            if (envFile.exists() && envFile.isFile()) {
                envFileSelector.addItem(envFile.getAbsolutePath());
            }
        }
        
        // Load the first file if available
        if (envFileSelector.getItemCount() > 0) {
            envFileSelector.setSelectedIndex(0);
        }
    }

    /**
     * Load a specific .env file
     */
    private void loadEnvFile(String path) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null) {
            List<EnvVariable> variables = envService.parseEnvFile(file);
            updateVariableGroups(variables);
        }
    }

    /**
     * Update the UI with variables organized by group
     */
    private void updateVariableGroups(List<EnvVariable> variables) {
        // Clear existing panels
        groupPanels.clear();
        
        // Get the env panel to update
        JPanel envPanel = (JPanel) ((JScrollPane) ((BorderLayout) getContentPanel()
                .getLayout()).getLayoutComponent(BorderLayout.CENTER))
                .getViewport().getView();
        
        envPanel.removeAll();
        
        // Group variables by group
        Map<String, List<EnvVariable>> groupedVariables = new HashMap<>();
        
        for (EnvVariable variable : variables) {
            groupedVariables.computeIfAbsent(variable.getGroup(), k -> new ArrayList<>())
                    .add(variable);
        }
        
        // Create panels for each group
        EnvProfile profile = envService.getActiveProfile();
        for (String group : profile.getAllGroups()) {
            List<EnvVariable> groupVars = groupedVariables.getOrDefault(group, Collections.emptyList());
            if (!groupVars.isEmpty()) {
                EnvGroupPanel groupPanel = new EnvGroupPanel(group, groupVars, envService, variableName -> {
                    // Handle variable selection or action
                    // For example, you might want to show details or allow editing
                    System.out.println("Variable selected: " + variableName);
                });
                groupPanels.put(group, groupPanel);
                envPanel.add(groupPanel);
            }
        }
        
        // Add "other" group last if it exists
        List<EnvVariable> otherVars = groupedVariables.getOrDefault("other", Collections.emptyList());
        if (!otherVars.isEmpty()) {
            EnvGroupPanel otherPanel = new EnvGroupPanel("other", otherVars, envService, variableName -> {
                // Handle variable selection or action for "other" group
                System.out.println("Variable selected from other group: " + variableName);
            });
            groupPanels.put("other", otherPanel);
            envPanel.add(otherPanel);
        }
        
        // Apply any active filter
        if (filterField != null && !filterField.getText().isEmpty()) {
            filterVariables();
        }
        
        envPanel.revalidate();
        envPanel.repaint();
    }

    /**
     * Filter variables based on the filter text
     */
    private void filterVariables() {
        String filterText = filterField.getText().toLowerCase();
        
        for (EnvGroupPanel panel : groupPanels.values()) {
            panel.applyFilter(filterText);
        }
    }
    
    /**
     * Update the UI when the profile changes
     */
    private void updateUI() {
        // Update project type label
        String projectType = ProjectDetector.getProjectType(project);
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
        while (tabbedPane.getTabCount() > 1) {
            tabbedPane.remove(1);
        }
        
        // Add Artisan tab if supported
        if (envService.getActiveProfile().supportsArtisanCommands()) {
            JPanel artisanPanel = createCliActionsPanel();
            tabbedPane.addTab("Artisan Commands", artisanPanel);
        }
        
        // Reload env files for the new profile
        loadEnvFiles();
        
        // Refresh UI
        mainPanel.revalidate();
        mainPanel.repaint();
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
