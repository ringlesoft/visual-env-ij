package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.model.EnvFileDefinition;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.services.EnvVariableService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Tab for displaying and editing environment variables
 */
public class EnvEditorTab extends JPanel {
    
    private final Project project;
    private final EnvVariableService envService;
    
    private JComboBox<String> envFileSelector;
    private JTextField filterField;
    private JPanel envVarsPanel;
    private final Map<String, EnvGroupPanel> groupPanels = new HashMap<>();
    
    /**
     * Create a new Environment editor tab
     *
     * @param project    The current project
     * @param envService The environment variable service
     */
    public EnvEditorTab(Project project, EnvVariableService envService) {
        this.project = project;
        this.envService = envService;
        
        setLayout(new BorderLayout());
        
        // Create the control panel (file selector, filter)
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Create the environment variables panel
        JPanel mainEnvPanel = createEnvPanel();
        add(mainEnvPanel, BorderLayout.CENTER);
        
        // Look for .env files in the project and load the first one found
        loadEnvFiles();
    }
    
    /**
     * Create the control panel with file selector and filter
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5));
        
        JPanel fileSelectorPanel = new JPanel(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JBLabel("Env File:"), BorderLayout.WEST);
        
        envFileSelector = new ComboBox<>();
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
        
        panel.add(fileSelectorPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * Create the main environment variables panel
     */
    private JPanel createEnvPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5));

        envVarsPanel = new JPanel();
        envVarsPanel.setLayout(new BoxLayout(envVarsPanel, BoxLayout.Y_AXIS));
        
        // Set preferred size to ensure scrolling works properly
        envVarsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane scrollPane = new JBScrollPane(envVarsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    
    /**
     * Load all available .env files in the project
     */
    public void loadEnvFiles() {
        String basePath = project.getBasePath();
        if (basePath == null) return;
        
        File baseDir = new File(basePath);
        if (!baseDir.exists() || !baseDir.isDirectory()) return;
        
        envFileSelector.removeAllItems();
        
        // Get common env filenames from the active profile
        String[] commonEnvFiles = envService.getActiveProfile().getCommonEnvFiles();
        List<EnvFileDefinition> envFileDefinitions = envService.getActiveProfile().getEnvFileDefinitions();
        
        // Look for these files in the project root
        for (EnvFileDefinition envFileDefinition : envFileDefinitions) {
            File envFile = new File(baseDir, envFileDefinition.getName());
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
        
        // Use the direct reference to the envVarsPanel
        envVarsPanel.removeAll();
        
        // Debug
        System.out.println("Updating variable groups with " + variables.size() + " variables");
        
        // Group variables by group
        Map<String, List<EnvVariable>> groupedVariables = new HashMap<>();
        
        for (EnvVariable variable : variables) {
            String group = variable.getGroup() != null ? variable.getGroup() : "other";
            groupedVariables.computeIfAbsent(group, k -> new ArrayList<>())
                    .add(variable);
        }
        
        // Create panels for each group
        EnvProfile profile = envService.getActiveProfile();
        boolean anyGroupAdded = false;
        
        for (String group : profile.getAllGroups()) {
            List<EnvVariable> groupVars = groupedVariables.getOrDefault(group, Collections.emptyList());
            if (!groupVars.isEmpty()) {
                System.out.println("Creating panel for group: " + group + " with " + groupVars.size() + " variables");
                EnvGroupPanel groupPanel = new EnvGroupPanel(group, groupVars, envService, variableName -> {
                    // Handle variable selection or action
                    // For example, you might want to show details or allow editing
                    System.out.println("Variable selected: " + variableName);
                });
                
                // Important: Configure component for BoxLayout
                groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                groupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, groupPanel.getPreferredSize().height));
                
                groupPanels.put(group, groupPanel);
                envVarsPanel.add(groupPanel);
                anyGroupAdded = true;
            }
        }
        
        // Add "other" group last if it exists
        List<EnvVariable> otherVars = groupedVariables.getOrDefault("other", Collections.emptyList());
        if (!otherVars.isEmpty()) {
            System.out.println("Creating panel for 'other' group with " + otherVars.size() + " variables");
            EnvGroupPanel otherPanel = new EnvGroupPanel("other", otherVars, envService, variableName -> {
                // Handle variable selection or action for "other" group
                System.out.println("Variable selected from other group: " + variableName);
            });
            
            // Important: Configure component for BoxLayout
            otherPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            otherPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, otherPanel.getPreferredSize().height));
            
            groupPanels.put("other", otherPanel);
            envVarsPanel.add(otherPanel);
            anyGroupAdded = true;
        }
        
        // If no variables were added, show a message
        if (!anyGroupAdded) {
            JLabel noVarsLabel = new JLabel("No environment variables found");
            noVarsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            envVarsPanel.add(noVarsLabel);
        }
        
        // Apply any active filter
        if (filterField != null && !filterField.getText().isEmpty()) {
            filterVariables();
        }
        
        envVarsPanel.revalidate();
        envVarsPanel.repaint();
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
     * Get the currently selected env file path
     * 
     * @return The selected file path or null if none selected
     */
    public String getSelectedFilePath() {
        return (String) envFileSelector.getSelectedItem();
    }
    
    /**
     * Select a specific env file
     * 
     * @param filePath The file path to select
     * @return true if the file was found and selected, false otherwise
     */
    public boolean selectEnvFile(String filePath) {
        for (int i = 0; i < envFileSelector.getItemCount(); i++) {
            String item = envFileSelector.getItemAt(i);
            if (item.equals(filePath)) {
                envFileSelector.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }
}
