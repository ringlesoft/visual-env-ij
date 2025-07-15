package com.ringlesoft.visualenv.toolWindow;

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
import java.util.*;
import java.util.List;

/**
 * Tab for executing CLI commands
 */
public class CliActionsTab extends JPanel {
    
    private final EnvVariableService envService;
    private final EnvProfile profile;
    private JTextArea resultArea; // Added field for displaying results
    
    /**
     * Create a new Artisan tab
     *
     * @param envService The environment variable service
     * @param profile The active environment profile
     */
    public CliActionsTab(EnvVariableService envService, EnvProfile profile) {
        this.envService = envService;
        this.profile = profile;
        
        setLayout(new BorderLayout());
        
        // Create the commands panel
        JPanel commandsPanel = createCommandsPanel();
        
        // Add the panel to the tab
        add(commandsPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create the panel with CLI command buttons
     *
     * @return The panel with CLI commands
     */
    private JPanel createCommandsPanel() {
        // Use a panel with BorderLayout for overall structure
        JPanel commandsPanel = new JPanel(new BorderLayout());
        
        // Create a panel with FlowLayout for horizontal button arrangement
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Add command buttons based on the profile's available CLI actions
        if (profile != null) {
            addProfileCommands(buttonsPanel);
        }
        
        // Add the buttons panel to the top of the main panel
        commandsPanel.add(buttonsPanel, BorderLayout.NORTH);
        
        // Create the result area for command output
        resultArea = new JTextArea(20, 80);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setBackground(UIManager.getColor("TextField.background"));
        resultArea.setText("Visual Env");
        
        // Add the result area in a scroll pane to the center of the panel
        JScrollPane resultScrollPane = new JBScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Command Results"));
        commandsPanel.add(resultScrollPane, BorderLayout.CENTER);
        
        return commandsPanel;
    }

    /**
     * Add command buttons from the profile's available CLI actions
     *
     * @param panel The panel to add buttons to
     */
    private void addProfileCommands(JPanel panel) {
        // Get CLI actions from the profile
        List<CliActionDefinition> actions = profile.getAvailableCliActions();
        
        if (actions.isEmpty()) {
            // If no actions available, show a message
            panel.add(new JBLabel("No CLI commands available for this profile"));
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
            
            // Add category header with a custom panel for each category
            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
            
            JBLabel categoryLabel = new JBLabel(category);
            categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
            categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            categoryPanel.add(categoryLabel);
            
            // Create a flow panel for buttons in this category
            JPanel buttonFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            buttonFlowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Add buttons for this category
            for (CliActionDefinition action : categoryActions) {
                JButton button = new JButton(action.getName());
                button.setToolTipText(action.getDescription());
                
                button.addActionListener(e -> {
                    executeCliAction(action);
                });
                
                buttonFlowPanel.add(button);
            }
            
            categoryPanel.add(buttonFlowPanel);
            panel.add(categoryPanel);
        }
    }
    
    /**
     * Execute a CLI action with parameters if required
     *
     * @param action The action to execute
     */
    private void executeCliAction(CliActionDefinition action) {
        String command = action.getCommand();
        String result = "";
        
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
     * @param title  The title for the result section
     */
    private void displayCommandResult(String result, String title) {
        // Update the result area with the new content and title
        resultArea.setText(result);
        resultArea.setCaretPosition(0); // Scroll to the top
    }
}
