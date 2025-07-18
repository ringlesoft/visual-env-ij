package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.listeners.EnvFileWatcher;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.services.EnvFileService;
import com.ringlesoft.visualenv.services.ProjectService;
import com.ringlesoft.visualenv.ui.VisualEnvTheme;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class VisualEnvToolWindowFactory implements ToolWindowFactory, AutoCloseable {

    private Project project;
    private EnvFileService envService;
    private ProjectService projectService;
    private JLabel projectTypeLabel;
    private Map<String, EnvProfile> availableProfiles;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JPanel controlPanel;
    private JPanel bottomPanel;
    private EnvFileWatcher envFileWatcher;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        this.envService = project.getService(EnvFileService.class);
        this.projectService = project.getService(ProjectService.class);
        
        // Initialize the project service and scan for .env files
        // This ensures files are scanned before the UI is created
        projectService.initialize();
        
        envFileWatcher = new EnvFileWatcher(project, this);
        envFileWatcher.startWatching();
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.removeAll();
        mainPanel.setMinimumSize(new Dimension(500, 500));

        createComponents();

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void createComponents() {
        // Check if there are any .env files in the project
        if (envService.getFileEnvVariables().isEmpty()) {
            // Show centered message when no .env files are found
            createEmptyStateUI();
        } else {
            // Show normal UI when .env files are available
            createNormalUI();
        }
    }

    /**
     * Create UI for when no .env files are found in the project
     */
    private void createEmptyStateUI() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel emptyLabel = new JBLabel("No .env files in this project");
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.PLAIN, 16f));
        emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        emptyPanel.add(emptyLabel, gbc);
        
        mainPanel.add(emptyPanel, BorderLayout.CENTER);
    }

    /**
     * Create normal UI when .env files are available
     */
    private void createNormalUI() {
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

        // Bottom actions
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(JBUI.Borders.empty(5));
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        if (envService.getActiveEnvFile() != null) {
            addAddVariableButton();
        }
    }

    /**
     * Create the control panel with profile selector and project type
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // set minimum width to 500
        panel.setMinimumSize(new Dimension(500, 0));
        panel.setBorder(JBUI.Borders.empty(5));

        JPanel topPanel = new JPanel(new GridLayout(1, 1, 0, 5));

        // First row - project type and profile selector
        JPanel projectTypePanel = new JPanel(new BorderLayout());

        // Add project type detection label
        String profileName = envService.getActiveProfile().getProfileName();
        projectTypeLabel = new JBLabel("Profile: " + profileName);
        projectTypeLabel.setForeground(VisualEnvTheme.TEXT_SECONDARY);
        projectTypeLabel.setBorder(JBUI.Borders.emptyLeft(5));
        projectTypePanel.add(projectTypeLabel, BorderLayout.WEST);

        topPanel.add(projectTypePanel);
        panel.add(topPanel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Create the CLI commands panel for the active profile
     */
    private JPanel createCliActionsPanel() {
        return new CliActionsTab(envService, envService.getActiveProfile());
    }

    /**
     * Update the UI when the profile changes
     */
    public void updateUI() {
        // Clear the main panel completely
        mainPanel.removeAll();
        
        // Check if there are any .env files and rebuild UI accordingly
        if (envService.getFileEnvVariables().isEmpty()) {
            // Show empty state UI
            createEmptyStateUI();
        } else {
            // Rebuild normal UI
            createNormalUI();
        }
        
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
            // Create a modal dialog for adding a new variable
            JDialog dialog = new JDialog();
            dialog.setTitle("Add New Variable to " + envService.getActiveEnvFile().getName());
            dialog.setModal(true);
            dialog.setLayout(new BorderLayout());

            // Main form panel with proper spacing
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(JBUI.Borders.empty(20));
            GridBagConstraints gbc = new GridBagConstraints();

            // Variable Name section
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = JBUI.insetsBottom(5);
            JLabel keyLabel = new JLabel("Variable Name:");
            formPanel.add(keyLabel, gbc);

            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = JBUI.insetsBottom(15);
            JTextField keyField = new JTextField(20);
            formPanel.add(keyField, gbc);

            // Value section
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.insets = JBUI.insetsBottom(5);
            JLabel valueLabel = new JLabel("Value:");
            formPanel.add(valueLabel, gbc);

            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = JBUI.insetsBottom(15);
            JTextField valueField = new JTextField(20);
            formPanel.add(valueField, gbc);

            // Message pane for notifications
            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = JBUI.emptyInsets();
            JLabel messagePane = new JLabel();
            messagePane.setText("");
            messagePane.setForeground(UIManager.getColor("Label.disabledForeground"));
            messagePane.setFont(messagePane.getFont().deriveFont(Font.ITALIC, messagePane.getFont().getSize() - 1f));
            formPanel.add(messagePane, gbc);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBorder(JBUI.Borders.empty(10, 15, 15, 15));

            JButton cancelButton = new JButton("Cancel");
            JButton saveButton = new JButton("Add");

            cancelButton.addActionListener(event -> dialog.dispose());
            saveButton.addActionListener(event -> {
                String key = keyField.getText();
                String value = valueField.getText();
                messagePane.setText("");
                if (key.isEmpty() || value.isEmpty()) {
                    return;
                }
                if (envService.addVariable(key, value)) {
                    dialog.dispose();
                } else {
                    messagePane.setText("Failed to add variable");
                }
            });

            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setSize(350, dialog.getHeight()); // Slightly wider for better proportions
            dialog.setLocationRelativeTo(null); // Center on screen

            // Focus the key field
            keyField.requestFocus();
            keyField.selectAll();
            dialog.setVisible(true);
        });
        bottomPanel.add(addButton);
    }

    public void addScanFilesButton() {
        JButton scanButton = new JButton("Scan .env files");
        scanButton.addActionListener(e -> {
            envService.rescanEnvFiles();
            updateUI();
        });
        bottomPanel.add(scanButton);
    }


    private JPanel getContentPanel() {
        return contentPanel;
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // Always make the tool window available
//        EnvFileService envService = project.getService(EnvFileService.class);
        return true;
    }

    @Override
    public void close() throws Exception {
        // Dispose everything held by this tool window
        envFileWatcher.stopWatching();
    }
}
