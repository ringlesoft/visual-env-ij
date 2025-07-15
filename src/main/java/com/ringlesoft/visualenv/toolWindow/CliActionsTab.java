package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.model.CliActionDefinition;
import com.ringlesoft.visualenv.model.CliParameterDefinition;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.services.EnvVariableService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for CLI command execution in the Visual Env tool window
 */
public class CliActionsTab extends JPanel {
    
    private final Project project;
    private final EnvVariableService envService;
    private final EnvProfile profile;

    /**
     * Create a new CLI actions panel
     *
     * @param project    The current project
     * @param envService The environment variable service
     */
    public CliActionsTab(Project project, EnvVariableService envService) {
        this.project = project;
        this.envService = envService;
        this.profile = envService.getActiveProfile();
        
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(5));
        
        JPanel commandsPanel = createCommandsPanel();
        add(new JBScrollPane(commandsPanel), BorderLayout.CENTER);
    }

    /**
     * Create the panel with CLI command buttons
     *
     * @return The panel with CLI commands
     */
    private JPanel createCommandsPanel() {
        JPanel commandsPanel = new JPanel();
        commandsPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add command buttons based on the profile's available CLI actions
        if (profile != null) {
            addProfileCommands(commandsPanel, gbc);
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
                displayCommandResult(result, "Command Result");
            }
        });
        commandsPanel.add(executeButton, gbc);
        
        return commandsPanel;
    }

    /**
     * Add command buttons from the profile's available CLI actions
     *
     * @param panel The panel to add buttons to
     * @param gbc   The grid bag constraints
     */
    private void addProfileCommands(JPanel panel, GridBagConstraints gbc) {
        // Get CLI actions from the profile
        List<CliActionDefinition> actions = profile.getAvailableCliActions();
        
        if (actions.isEmpty()) {
            // If no actions available, show a message
            panel.add(new JBLabel("No CLI commands available for this profile"), gbc);
            return;
        }
        
        // Group actions by category if available
        Map<String, List<CliActionDefinition>> categorizedActions = new HashMap<>();
        
        for (CliActionDefinition action : actions) {
            String category = action.getCategory();
            if (category == null || category.isEmpty()) {
                category = "General";
            }
            
            if (!categorizedActions.containsKey(category)) {
                categorizedActions.put(category, new ArrayList<>());
            }
            categorizedActions.get(category).add(action);
        }
        
        // Add actions by category
        for (Map.Entry<String, List<CliActionDefinition>> entry : categorizedActions.entrySet()) {
            String category = entry.getKey();
            List<CliActionDefinition> categoryActions = entry.getValue();
            
            // Add category header
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 3;
            JBLabel categoryLabel = new JBLabel(category);
            categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
            panel.add(categoryLabel, gbc);
            
            // Reset for buttons
            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.gridx = 0;
            
            // Add buttons for this category
            for (CliActionDefinition action : categoryActions) {
                JButton button = new JButton(action.getName());
                button.setToolTipText(action.getDescription());
                
                button.addActionListener(e -> {
                    executeCliAction(action);
                });
                
                panel.add(button, gbc);
                gbc.gridx++;
                
                if (gbc.gridx > 2) {
                    gbc.gridx = 0;
                    gbc.gridy++;
                }
            }
            
            // Add spacing after category
            gbc.gridx = 0;
            gbc.gridy++;
        }
    }
    
    /**
     * Execute a CLI action with parameters if required
     *
     * @param action The CLI action to execute
     */
    private void executeCliAction(CliActionDefinition action) {
        String command = action.getCommand();
        String result;
        
        // Handle actions that require user input
        if (action.isRequiresUserInput()) {
            // Get parameters
            List<CliParameterDefinition> parameters = action.getParameters();
            if (parameters != null && !parameters.isEmpty()) {
                Map<String, String> paramValues = new HashMap<>();
                
                // Collect parameter values
                for (CliParameterDefinition param : parameters) {
                    String value = promptForParameter(param);
                    if (value == null) {
                        // User cancelled
                        return;
                    }
                    paramValues.put(param.getName(), value);
                }
                
                // Replace parameters in command
                for (Map.Entry<String, String> entry : paramValues.entrySet()) {
                    command = command.replace("${" + entry.getKey() + "}", entry.getValue());
                }
            }
        }
        
        // Execute the command
        result = envService.executeArtisanCommand(command);
        displayCommandResult(result, action.getName() + " Result");
    }
    
    /**
     * Prompt for a parameter value
     *
     * @param param The parameter definition
     * @return The user input or null if cancelled
     */
    private String promptForParameter(CliParameterDefinition param) {
        return JOptionPane.showInputDialog(
                this,
                "Enter value for " + param.getName() + ":",
                "Parameter Input",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Display the result of a command execution
     *
     * @param result The command result text
     * @param title  The title for the result dialog
     */
    private void displayCommandResult(String result, String title) {
        JTextArea textArea = new JTextArea(result);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
