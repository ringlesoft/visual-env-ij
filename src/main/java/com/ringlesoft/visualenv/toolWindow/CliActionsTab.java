package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.profile.EnvProfile;
import com.ringlesoft.visualenv.services.EnvVariableService;

import javax.swing.*;
import java.awt.*;

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
        
        // Add command buttons based on the profile
        if (profile.supportsArtisanCommands()) {
            addArtisanCommands(commandsPanel, gbc);
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
     * Add Artisan command buttons for Laravel projects
     *
     * @param panel The panel to add buttons to
     * @param gbc   The grid bag constraints
     */
    private void addArtisanCommands(JPanel panel, GridBagConstraints gbc) {
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
                    String name = promptForName(command);
                    if (name == null || name.trim().isEmpty()) {
                        return;
                    }
                    
                    result = envService.executeArtisanCommand(command + " " + name);
                } else {
                    result = envService.executeArtisanCommand(command);
                }
                
                displayCommandResult(result, "Artisan Command Result");
            });
            
            panel.add(button, gbc);
            gbc.gridx++;
            
            if (gbc.gridx > 2) {
                gbc.gridx = 0;
                gbc.gridy++;
            }
        }
    }

    /**
     * Prompt the user for a name input (for controllers, models, etc.)
     *
     * @param command The command being executed
     * @return The user's input or null if cancelled
     */
    private String promptForName(String command) {
        String type = command.equals("make:controller") ? "controller" : "model";
        String title = "Create " + (command.equals("make:controller") ? "Controller" : "Model");
        
        return JOptionPane.showInputDialog(
                this,
                "Enter the name for the " + type + ":",
                title,
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
