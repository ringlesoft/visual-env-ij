package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
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
import com.ringlesoft.visualenv.utils.EnvFileManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.ringlesoft.visualenv.ui.VisualEnvTheme;

/**
 * Tab for displaying and editing environment variables
 */
public class EnvEditorTab extends JPanel {
    
    private final Project project;
    private final EnvVariableService envVariableService;
    
    private JComboBox<String> envFileSelector;
    private JTextField filterField;
    private JPanel envVarsPanel;
    private final Map<String, EnvGroupPanel> groupPanels = new HashMap<>();
    private VirtualFile selectedEnvFile;
    private final Map<String, String> fileBasenameToPath = new HashMap<>();

    /**
     * Create a new Environment editor tab
     *
     * @param project    The current project
     * @param envVariableService The environment variable service
     */
    public EnvEditorTab(Project project, EnvVariableService envVariableService) {
        this.project = project;
        this.envVariableService = envVariableService;
        
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
        // Create a panel with custom layout to achieve the 3:2:1 ratio
        JPanel fileSelectorPanel = new JPanel();
        fileSelectorPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insetsRight(5);  // Add some spacing between components
        
        // Filter field taking 3/6 of space
        filterField = new JBTextField();
        filterField.putClientProperty("placeholder.text", "Search");
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;  // 3/6 of space
        fileSelectorPanel.add(filterField, gbc);
        
        // File label and dropdown taking 2/6 of space
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
//        filePanel.add(new JBLabel("File:"), BorderLayout.WEST);
        
        envFileSelector = new ComboBox<>();
        envFileSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) envFileSelector.getSelectedItem();
                if (selected != null) {
                    selectedEnvFile = LocalFileSystem.getInstance().findFileByPath(fileBasenameToPath.getOrDefault(selected, selected));
                    loadEnvFile(selected);
                }
            }
        });
        filePanel.add(envFileSelector, BorderLayout.CENTER);
        
        gbc.gridx = 1;
        gbc.weightx = 0.2;  // 2/6 of space
        fileSelectorPanel.add(filePanel, gbc);
        
        // Refresh button taking 1/6 of space
        JButton refreshButton = new JButton();
        refreshButton.setText("↻");
        refreshButton.setToolTipText("Refresh environment files");
        refreshButton.setForeground(VisualEnvTheme.PRIMARY);
        refreshButton.addActionListener(e -> loadEnvFiles());
        
        gbc.gridx = 2;
        gbc.weightx = 0.17;  // 1/6 of space
        gbc.insets = JBUI.emptyInsets();  // Remove right margin for last component
        fileSelectorPanel.add(refreshButton, gbc);
        
        panel.add(fileSelectorPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * Create the main environment variables panel
     */
    private JPanel createEnvPanel() {
        // Main panel with BorderLayout to properly handle scrolling
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Content panel that will hold all the environment group panels
        envVarsPanel = new JPanel();
        envVarsPanel.setLayout(new BoxLayout(envVarsPanel, BoxLayout.Y_AXIS));
        
        // Configure scrolling
        JScrollPane scrollPane = new JBScrollPane(envVarsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        
        // Add the scroll pane to the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
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
        fileBasenameToPath.clear();
        
        // Get common env filenames from the active profile
        String[] commonEnvFiles = envVariableService.getActiveProfile().getCommonEnvFiles();
        List<EnvFileDefinition> envFileDefinitions = envVariableService.getActiveProfile().getEnvFileDefinitions();
        
        // Look for these files in the project root
        for (EnvFileDefinition envFileDefinition : envFileDefinitions) {
            File envFile = new File(baseDir, envFileDefinition.getName());
            if (envFile.exists() && envFile.isFile()) {
                String absolutePath = envFile.getAbsolutePath();
                String basename = envFile.getName();
                fileBasenameToPath.put(basename, absolutePath);
                envFileSelector.addItem(basename);
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
    private void loadEnvFile(String filenameOrPath) {
        String path = fileBasenameToPath.getOrDefault(filenameOrPath, filenameOrPath);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null) {
            List<EnvVariable> variables = envVariableService.parseEnvFile(file);
            updateVariableGroups(variables);
        }
    }
    
    /**
     * Update the UI with variables organized by group
     */
    private void updateVariableGroups(List<EnvVariable> variables) {
        // Group variables by group name
        Map<String, List<EnvVariable>> groupedVars = variables.stream()
                .collect(Collectors.groupingBy(EnvVariable::getGroup));

        // Add panels for each group
        envVarsPanel.removeAll();
        groupPanels.clear();

        for (Map.Entry<String, List<EnvVariable>> entry : groupedVars.entrySet()) {
            String groupName = entry.getKey();
            List<EnvVariable> groupVars = entry.getValue();

            EnvGroupPanel groupPanel = new EnvGroupPanel(groupName, groupVars, envVariableService, this::updateStatus, this);
            groupPanels.put(groupName, groupPanel);
            envVarsPanel.add(groupPanel);
        }

        // Apply current filter
        filterVariables();

        // Fix visibility issues
        envVarsPanel.revalidate();
        envVarsPanel.repaint();
    }

    private void updateStatus(String s) {

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
        String selected = (String) envFileSelector.getSelectedItem();
        if (selected != null) {
            return fileBasenameToPath.getOrDefault(selected, selected);
        }
        return null;
    }
    
    /**
     * Select a specific env file
     * 
     * @param filePath The file path to select
     * @return true if the file was found and selected, false otherwise
     */
    public boolean selectEnvFile(String filePath) {
        // First check if the full path matches any stored path
        for (Map.Entry<String, String> entry : fileBasenameToPath.entrySet()) {
            if (entry.getValue().equals(filePath)) {
                for (int i = 0; i < envFileSelector.getItemCount(); i++) {
                    if (envFileSelector.getItemAt(i).equals(entry.getKey())) {
                        envFileSelector.setSelectedIndex(i);
                        return true;
                    }
                }
            }
        }
        
        // Then try matching by item directly (for backward compatibility)
        for (int i = 0; i < envFileSelector.getItemCount(); i++) {
            String item = envFileSelector.getItemAt(i);
            if (item.equals(filePath)) {
                envFileSelector.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new environment variable to the currently selected env file
     */
    public void addEnvironmentVariable(String key, String value) {
        if (selectedEnvFile != null) {
            EnvFileManager.setEnvVariable(project, selectedEnvFile, key, value);
            reloadCurrentEnvFile();
        }
    }

    /**
     * Updates an existing environment variable in the currently selected env file
     */
    public void updateEnvironmentVariable(String key, String value) {
        if (selectedEnvFile != null) {
            EnvFileManager.setEnvVariable(project, selectedEnvFile, key, value);
            reloadCurrentEnvFile();
        }
    }

    /**
     * Removes an environment variable from the currently selected env file
     */
    public void removeEnvironmentVariable(String key) {
        if (selectedEnvFile != null) {
            EnvFileManager.removeEnvVariable(project, selectedEnvFile, key);
            reloadCurrentEnvFile();
        }
    }

    /**
     * Get the value of an environment variable from the currently selected env file
     */
    public String getEnvironmentVariableValue(String key) {
        if (selectedEnvFile != null) {
            return EnvFileManager.getEnvVariable(selectedEnvFile, key);
        }
        return null;
    }

    /**
     * Updates multiple environment variables at once
     */
    public void updateMultipleEnvironmentVariables(Map<String, String> variables) {
        if (selectedEnvFile != null) {
            EnvFileManager.setMultipleEnvVariables(project, selectedEnvFile, variables);
            reloadCurrentEnvFile();
        }
    }
    
    /**
     * Reload the currently selected environment file
     */
    private void reloadCurrentEnvFile() {
        if (selectedEnvFile != null) {
            loadEnvFile(selectedEnvFile.getPath());
        }
    }
}
