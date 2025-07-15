package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.VisualEnvBundle;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;
import com.ringlesoft.visualenv.services.EnvVariableService;
import com.ringlesoft.visualenv.utils.ProjectDetector;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tool window factory for Visual Env plugin.
 */
public class VisualEnvToolWindowFactory implements ToolWindowFactory {
    private static final Logger LOG = Logger.getInstance(VisualEnvToolWindowFactory.class);
    private boolean isLaravelProject = false;

    public VisualEnvToolWindowFactory() {
        LOG.info("VisualEnvToolWindowFactory initialized");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Check if this is a Laravel project
        isLaravelProject = ProjectDetector.isLaravelProject(project);
        LOG.info("Project detected as Laravel: " + isLaravelProject);

        VisualEnvToolWindow visualEnvToolWindow = new VisualEnvToolWindow(toolWindow, project, isLaravelProject);
        Content content = ContentFactory.getInstance().createContent(
                visualEnvToolWindow.getContent(),
                VisualEnvBundle.message("visualenv.toolwindow.title"),
                false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // return isLaravelProject; // Just allow it for now
        return true;
    }

    /**
     * Inner class representing the tool window UI.
     */
    private static class VisualEnvToolWindow {
        private final Project project;
        private final EnvVariableService envVariableService;
        private final EnvVariableTableModel fileEnvModel = new EnvVariableTableModel();
        private final boolean isLaravelProject;

        private JPanel envFilePanel;
        private JCheckBox showAllVariablesCheckbox;
        private JLabel statusLabel;
        private JLabel fileStatusLabel;
        private JLabel projectTypeLabel;

        public VisualEnvToolWindow(ToolWindow toolWindow, Project project, boolean isLaravelProject) {
            this.project = project;
            this.envVariableService = project.getService(EnvVariableService.class);
            this.isLaravelProject = isLaravelProject;
        }

        public JComponent getContent() {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(JBUI.Borders.empty(10));

            // Create the project type indicator
            projectTypeLabel = new JBLabel(isLaravelProject ?
                    "Laravel Project Detected" :
                    "Standard Project (Laravel-specific features disabled)");
            projectTypeLabel.setBorder(JBUI.Borders.emptyBottom(10));
            projectTypeLabel.setForeground(isLaravelProject ?
                    new JBColor(new Color(0, 128, 0), new Color(0, 150, 0)) :
                    new JBColor(new Color(128, 128, 128), new Color(160, 160, 160)));
            projectTypeLabel.setFont(projectTypeLabel.getFont().deriveFont(Font.ITALIC));

            mainPanel.add(projectTypeLabel, BorderLayout.NORTH);

            // Create tabbed pane for different views
            JTabbedPane tabbedPane = new JTabbedPane();

            // Create the main content with environment file variables
            JPanel fileEnvPanel = createFileEnvPanel();
            tabbedPane.addTab(VisualEnvBundle.message("visualenv.fileenv.tab"), fileEnvPanel);

            // Only add Artisan tab for Laravel projects
            if (isLaravelProject) {
                // Artisan Commands tab for Laravel projects
                JPanel artisanPanel = createArtisanPanel();
                tabbedPane.addTab(VisualEnvBundle.message("visualenv.artisan.tab"), artisanPanel);
            }

            mainPanel.add(tabbedPane, BorderLayout.CENTER);

            // Try to load .env file automatically
            autoLoadEnvFile();

            return mainPanel;
        }

        private void autoLoadEnvFile() {
            String basePath = project.getBasePath();
            if (basePath != null) {
                VirtualFile envFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, ".env").toString());
                if (envFile != null && envFile.exists()) {
                    loadEnvFile(envFile);
                } else {
                    // Check for .env.example file in Laravel projects to offer creation
                    if (isLaravelProject) {
                        VirtualFile envExampleFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, ".env.example").toString());
                        if (envExampleFile != null && envExampleFile.exists()) {
                            offerCreateEnvFromExample(envExampleFile);
                        }
                    }
                }
            }
        }

        private void offerCreateEnvFromExample(VirtualFile envExampleFile) {
            if (statusLabel != null) {
                statusLabel.setText("No .env file found, but .env.example exists. Use 'Create from Example' option.");
            }
            // Creation functionality would be implemented here or through a notification
        }

        private JPanel createFileEnvPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(new EmptyBorder(10, 0, 10, 0));

            // Top controls panel
            JPanel controlsPanel = new JPanel(new BorderLayout(5, 0));

            // File selector and open button
            JPanel filePanel = new JPanel(new BorderLayout(5, 0));
            JBLabel fileLabel = new JBLabel("File: ");
            fileLabel.setPreferredSize(new Dimension(35, fileLabel.getPreferredSize().height));
            JBTextField fileField = new JBTextField();
            fileField.setEditable(false);

            JButton openFileButton = new JButton(VisualEnvBundle.message("visualenv.open.file"));
            openFileButton.addActionListener(e -> chooseAndParseEnvFile());

            // Add Create from Example button for Laravel projects
            JButton createFromExampleButton = new JButton("Create from Example");
            createFromExampleButton.addActionListener(e -> createEnvFromExample());
            createFromExampleButton.setEnabled(isLaravelProject);
            createFromExampleButton.setToolTipText(isLaravelProject ?
                    "Create a .env file from .env.example" :
                    "This feature is only available in Laravel projects");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.add(createFromExampleButton);
            buttonPanel.add(openFileButton);

            filePanel.add(fileLabel, BorderLayout.WEST);
            filePanel.add(fileField, BorderLayout.CENTER);
            filePanel.add(buttonPanel, BorderLayout.EAST);

            // Filter field
            JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
            JBLabel filterLabel = new JBLabel("Filter: ");
            filterLabel.setPreferredSize(new Dimension(35, filterLabel.getPreferredSize().height));
            JBTextField filterField = new JBTextField();

            // Show/hide non-predefined variables checkbox
            showAllVariablesCheckbox = new JCheckBox("Show all variables", true);
            showAllVariablesCheckbox.addActionListener(e -> refreshEnvFilePanel());

            filterPanel.add(filterLabel, BorderLayout.WEST);
            filterPanel.add(filterField, BorderLayout.CENTER);
            filterPanel.add(showAllVariablesCheckbox, BorderLayout.EAST);

            controlsPanel.add(filePanel, BorderLayout.NORTH);
            controlsPanel.add(filterPanel, BorderLayout.SOUTH);
            panel.add(controlsPanel, BorderLayout.NORTH);

            // Create content panel for env file variables
            envFilePanel = new JPanel();
            envFilePanel.setLayout(new BoxLayout(envFilePanel, BoxLayout.Y_AXIS));

            // Create scroll panel
            JBScrollPane scrollPane = new JBScrollPane(envFilePanel);
            panel.add(scrollPane, BorderLayout.CENTER);

            // Status bar
            JPanel statusPanel = new JPanel(new BorderLayout());
            statusLabel = new JBLabel("No .env file loaded");
            fileStatusLabel = new JBLabel(VisualEnvBundle.message("visualenv.no.variables"));

            JButton newVarButton = new JButton("Add Variable");
            newVarButton.addActionListener(e -> showAddVariableDialog());

            statusPanel.add(statusLabel, BorderLayout.WEST);
            statusPanel.add(fileStatusLabel, BorderLayout.CENTER);
            statusPanel.add(newVarButton, BorderLayout.EAST);
            panel.add(statusPanel, BorderLayout.SOUTH);

            // Setup filter listener
            filterField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    filterEnvVariables(filterField.getText());
                }
            });

            return panel;
        }

        private void createEnvFromExample() {
            String basePath = project.getBasePath();
            if (basePath != null) {
                VirtualFile envExampleFile = LocalFileSystem.getInstance().findFileByPath(Path.of(basePath, ".env.example").toString());
                if (envExampleFile != null && envExampleFile.exists()) {
                    boolean result = envVariableService.createEnvFromExample(envExampleFile);
                    if (result) {
                        showBalloon("Created .env file from .env.example", MessageType.INFO);
                        // Try to load the newly created .env file
                        autoLoadEnvFile();
                    } else {
                        showBalloon("Failed to create .env file", MessageType.ERROR);
                    }
                } else {
                    showBalloon("No .env.example file found", MessageType.WARNING);
                }
            }
        }

        private JPanel createArtisanPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(new EmptyBorder(10, 0, 10, 0));

            // Commands panel
            JPanel commandsPanel = new JPanel();
            commandsPanel.setLayout(new BoxLayout(commandsPanel, BoxLayout.Y_AXIS));
            commandsPanel.setBorder(JBUI.Borders.empty(10));

            // Key:generate command
            JPanel keyGenPanel = new JPanel(new BorderLayout(10, 0));
            keyGenPanel.setBorder(JBUI.Borders.emptyBottom(10));

            JBLabel keyGenLabel = new JBLabel("Generate Application Key:");
            keyGenLabel.setToolTipText("Generates a new encryption key for your application.");

            JButton keyGenButton = new JButton("Run php artisan key:generate");
            keyGenButton.addActionListener(e -> executeArtisanCommand("key:generate"));

            keyGenPanel.add(keyGenLabel, BorderLayout.WEST);
            keyGenPanel.add(keyGenButton, BorderLayout.EAST);

            // Add commands
            commandsPanel.add(keyGenPanel);

            // Add migrate commands
            JPanel migratePanel = new JPanel(new BorderLayout(10, 0));
            migratePanel.setBorder(JBUI.Borders.emptyBottom(10));

            JBLabel migrateLabel = new JBLabel("Run Database Migrations:");
            migrateLabel.setToolTipText("Run the database migrations.");

            JButton migrateButton = new JButton("Run php artisan migrate");
            migrateButton.addActionListener(e -> executeArtisanCommand("migrate"));

            migratePanel.add(migrateLabel, BorderLayout.WEST);
            migratePanel.add(migrateButton, BorderLayout.EAST);

            commandsPanel.add(migratePanel);

            // Add cache commands
            JPanel cachePanel = new JPanel(new BorderLayout(10, 0));
            cachePanel.setBorder(JBUI.Borders.emptyBottom(10));

            JBLabel cacheLabel = new JBLabel("Clear Application Cache:");
            cacheLabel.setToolTipText("Clear the application cache.");

            JButton cacheButton = new JButton("Run php artisan cache:clear");
            cacheButton.addActionListener(e -> executeArtisanCommand("cache:clear"));

            cachePanel.add(cacheLabel, BorderLayout.WEST);
            cachePanel.add(cacheButton, BorderLayout.EAST);

            commandsPanel.add(cachePanel);

            // Output panel
            JTextArea outputArea = new JTextArea();
            outputArea.setEditable(false);
            JBScrollPane outputScrollPane = new JBScrollPane(outputArea);
            outputScrollPane.setPreferredSize(new Dimension(600, 300));

            // Assemble
            panel.add(commandsPanel, BorderLayout.NORTH);
            panel.add(outputScrollPane, BorderLayout.CENTER);

            return panel;
        }

        private void executeArtisanCommand(String command) {
            if (envVariableService.getActiveEnvFile() == null) {
                showBalloon("Please load a .env file first", MessageType.WARNING);
                return;
            }

            String result = envVariableService.executeArtisanCommand(command);
            showBalloon("Command executed: " + command,
                    result.toLowerCase().contains("error") ? MessageType.ERROR : MessageType.INFO);

            // Refresh the env file panel to show updated values
            refreshEnvFilePanel();
        }

        private void showAddVariableDialog() {
            if (envVariableService.getActiveEnvFile() == null) {
                showBalloon("Please load a .env file first", MessageType.WARNING);
                return;
            }

            // Create dialog
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(JBUI.Borders.empty(10));

            // Variable name
            JPanel namePanel = new JPanel(new BorderLayout(5, 0));
            JBLabel nameLabel = new JBLabel("Variable Name:");
            JBTextField nameField = new JBTextField();
            namePanel.add(nameLabel, BorderLayout.WEST);
            namePanel.add(nameField, BorderLayout.CENTER);

            // Variable value
            JPanel valuePanel = new JPanel(new BorderLayout(5, 0));
            JBLabel valueLabel = new JBLabel("Variable Value:");
            JBTextField valueField = new JBTextField();
            valuePanel.add(valueLabel, BorderLayout.WEST);
            valuePanel.add(valueField, BorderLayout.CENTER);

            // Add to panel
            panel.add(namePanel, BorderLayout.NORTH);
            panel.add(valuePanel, BorderLayout.CENTER);

            // Show dialog
            int result = JOptionPane.showConfirmDialog(
                    null, panel, "Add Environment Variable",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String value = valueField.getText();

                if (!name.isEmpty()) {
                    boolean success = envVariableService.updateEnvVariable(name, value);
                    if (success) {
                        refreshEnvFilePanel();
                        showBalloon("Variable " + name + " added successfully", MessageType.INFO);
                    } else {
                        showBalloon("Failed to add variable " + name, MessageType.ERROR);
                    }
                }
            }
        }

        private void chooseAndParseEnvFile() {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle("Select .env File")
                    .withDescription("Choose a .env file to parse")
                    .withFileFilter(file -> file.getName().contains(".env") || file.getName().endsWith(".properties"));

            VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, null);
            if (files.length > 0) {
                loadEnvFile(files[0]);
            }
        }

        private void loadEnvFile(VirtualFile file) {
            List<EnvVariable> fileVariables = envVariableService.parseEnvFile(file);
            fileEnvModel.setVariables(fileVariables);

            if (fileStatusLabel != null) {
                if (fileVariables.isEmpty()) {
                    fileStatusLabel.setText(VisualEnvBundle.message("visualenv.no.variables"));
                } else {
                    fileStatusLabel.setText(fileVariables.size() + " variables from " + file.getName());
                }
            }

            if (statusLabel != null) {
                statusLabel.setText("Loaded: " + file.getPath());
            }

            refreshEnvFilePanel();
        }

        private void refreshEnvFilePanel() {
            if (envFilePanel == null) return;

            // Clear panel
            envFilePanel.removeAll();

            // Get all variables
            List<EnvVariable> variables = new ArrayList<>();

            Map<String, List<EnvVariable>> fileEnvVariables = envVariableService.getAllFileEnvVariables();
            for (List<EnvVariable> varList : fileEnvVariables.values()) {
                variables.addAll(varList);
            }

            // Filter out non-predefined variables if checkbox is unchecked
            if (!showAllVariablesCheckbox.isSelected()) {
                variables = variables.stream()
                        .filter(var -> EnvVariableRegistry.isPredefined(var.getName()))
                        .collect(Collectors.toList());
            }

            // Group variables by group
            Map<String, List<EnvVariable>> groupedVars = variables.stream()
                    .collect(Collectors.groupingBy(EnvVariable::getGroup));

            // Create panels for each group
            for (Map.Entry<String, List<EnvVariable>> entry : groupedVars.entrySet()) {
                EnvGroupPanel groupPanel = new EnvGroupPanel(
                        entry.getKey(),
                        entry.getValue(),
                        envVariableService,
                        message -> {
                            if (statusLabel != null) {
                                statusLabel.setText(message);
                            }
                        }
                );

                envFilePanel.add(groupPanel);
            }

            // Update UI
            envFilePanel.revalidate();
            envFilePanel.repaint();
        }

        private void filterEnvVariables(String filterText) {
            // Implementation of filtering logic
            if (filterText == null || filterText.trim().isEmpty()) {
                refreshEnvFilePanel();
                return;
            }

            // This is a simple implementation - a more sophisticated one would update visibility of elements in place
            String lowerCaseFilter = filterText.toLowerCase();
            for (Component comp : envFilePanel.getComponents()) {
                if (comp instanceof EnvGroupPanel) {
                    EnvGroupPanel groupPanel = (EnvGroupPanel) comp;
                    groupPanel.filterVariables(lowerCaseFilter);
                }
            }
        }

        private void showBalloon(String message, MessageType messageType) {
            JBPopupFactory.getInstance()
                    .createBalloonBuilder(new JLabel(message))
                    .setFillColor(messageType == MessageType.ERROR ?
                            JBColor.RED : messageType == MessageType.WARNING ?
                            JBColor.YELLOW : JBColor.WHITE)
                    .createBalloon()
                    .show(RelativePoint.getNorthEastOf(envFilePanel), Balloon.Position.above);
        }
    }
}
