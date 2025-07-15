package com.ringlesoft.visualenv.toolWindow;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;
import com.ringlesoft.visualenv.services.EnvVariableService;
import com.ringlesoft.visualenv.utils.EnvFileManager;

import javax.swing.*;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panel that displays environment variables grouped by category
 */
public class EnvGroupPanel extends JPanel {
    
    private final String groupName;
    private List<EnvVariable> variables;
    private final EnvVariableService envVariableService;
    private final Consumer<String> statusUpdater;
    private final Map<String, Component> variableComponents = new HashMap<>();
    private final JPanel variablesPanel;
    private final JLabel titleLabel;
    private boolean expanded = true;
    private EnvEditorTab parentTab;
    private String currentFilter;

    public EnvGroupPanel(String groupName, List<EnvVariable> variables, EnvVariableService envVariableService, Consumer<String> statusUpdater, EnvEditorTab parentTab) {
        this.groupName = groupName;
        this.variables = variables;
        this.envVariableService = envVariableService;
        this.statusUpdater = statusUpdater;
        this.parentTab = parentTab;
        
        // Setup panel
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(5));
        
        // Create header with expand/collapse
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                JBUI.Borders.empty(5)
        ));
        
        // Add expand/collapse icon
        JLabel expandIcon = new JLabel("▼");
        expandIcon.setPreferredSize(new Dimension(20, 20));
        headerPanel.add(expandIcon, BorderLayout.WEST);
        
        // Group name and count
        titleLabel = new JLabel(groupName + " (" + variables.size() + ")");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Make the header clickable for expand/collapse
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                expanded = !expanded;
                expandIcon.setText(expanded ? "▼" : "▶");
                variablesPanel.setVisible(expanded);
                revalidate();
                repaint();
            }
        });
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create panel for variables
        variablesPanel = new JPanel();
        variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));
        variablesPanel.setBorder(JBUI.Borders.empty(10, 20, 5, 5));
        
        // Add each variable control
        for (int i = 0; i < variables.size(); i++) {
            EnvVariable variable = variables.get(i);
            JPanel varPanel = createControlForVariable(variable);
            createActions(varPanel, variable, i);
            variablesPanel.add(varPanel);
        }
        
        addAddVariableButton();
        
        // Add to scroll pane to handle overflow
        JScrollPane scrollPane = new JScrollPane(variablesPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createControlForVariable(EnvVariable variable) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(JBUI.Borders.emptyBottom(5));
        
        // Create variable name label
        JLabel nameLabel = new JLabel(variable.getName() + ":");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        nameLabel.setBorder(JBUI.Borders.empty(0, 0, 0, 10));
        nameLabel.setToolTipText(getDescriptionForVariable(variable));
        panel.add(nameLabel, BorderLayout.WEST);
        
        // Create variable value component based on type
        Component valueComponent = createControlByType(variable);
        panel.add(valueComponent, BorderLayout.CENTER);

        // Store reference to the component for filtering
        variableComponents.put(variable.getName(), panel);
        
        return panel;
    }
    
    private void createActions(JPanel panel, EnvVariable variable, int variableRow) {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        
        // Edit button
        JButton editButton = new JButton("Edit");
        editButton.setToolTipText("Edit this environment variable");
        editButton.addActionListener(e -> showEditDialog(variable));
        actionsPanel.add(editButton);
        
        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setToolTipText("Delete this environment variable");
        deleteButton.addActionListener(e -> showDeleteConfirmation(variable));
        actionsPanel.add(deleteButton);
        
        panel.add(actionsPanel, BorderLayout.EAST);
    }
    
    private Component createControlByType(EnvVariable variable) {
        EnvVariableDefinition definition = EnvVariableRegistry.getDefinition(variable.getName());
        
        if (definition != null) {
            switch (definition.getType()) {
                case BOOLEAN:
                    return createBooleanControl(variable);
                case DROPDOWN:
                    return createDropdownControl(variable, definition.getPossibleValues());
                case INTEGER:
                    return createIntegerControl(variable);
                default:
                    return createTextControl(variable, variable.isSecret());
            }
        } else {
            // Default to text input
            return createTextControl(variable, variable.isSecret());
        }
    }
    
    private Component createTextControl(EnvVariable variable, boolean isSecret) {
        JTextField textField;
        
        if (isSecret) {
            JPasswordField passwordField = new JPasswordField(variable.getValue());
            textField = passwordField;
        } else {
            textField = new JTextField(variable.getValue());
        }
        
        textField.addActionListener(e -> updateVariable(variable.getName(), textField.getText()));
        
        return textField;
    }
    
    private Component createBooleanControl(EnvVariable variable) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected("true".equalsIgnoreCase(variable.getValue()) || 
                             "1".equals(variable.getValue()) || 
                             "yes".equalsIgnoreCase(variable.getValue()));
        
        checkBox.addItemListener(e -> {
            boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;
            updateVariable(variable.getName(), isSelected ? "true" : "false");
        });
        
        return checkBox;
    }
    
    private Component createDropdownControl(EnvVariable variable, List<String> options) {
        JComboBox<String> comboBox = new JComboBox<>();
        for (String option : options) {
            comboBox.addItem(option);
        }
        
        // Set selected item if it exists
        if (options.contains(variable.getValue())) {
            comboBox.setSelectedItem(variable.getValue());
        }
        
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateVariable(variable.getName(), (String) comboBox.getSelectedItem());
            }
        });
        
        return comboBox;
    }
    
    private Component createIntegerControl(EnvVariable variable) {
        try {
            int value = Integer.parseInt(variable.getValue());
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
            
            spinner.addChangeListener(e -> {
                updateVariable(variable.getName(), spinner.getValue().toString());
            });
            
            return spinner;
        } catch (NumberFormatException e) {
            // If not a valid number, fall back to text control
            return createTextControl(variable, false);
        }
    }
    
    private String getDescriptionForVariable(EnvVariable variable) {
        EnvVariableDefinition definition = EnvVariableRegistry.getDefinition(variable.getName());
        return definition != null ? definition.getDescription() : "";
    }
    
    private void updateVariable(String name, String value) {
        boolean success = envVariableService.updateEnvVariable(name, value);
        if (success) {
            statusUpdater.accept("Updated " + name + " to " + (isSecretVariable(name) ? "*****" : value));
        } else {
            statusUpdater.accept("Failed to update " + name);
        }
    }
    
    private boolean isSecretVariable(String name) {
        EnvVariableDefinition definition = EnvVariableRegistry.getDefinition(name);
        return definition != null && definition.isSecret();
    }
    
    /**
     * Filter variables based on search text
     * @param filterText The text to filter by
     */
    private void filterVariables(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            // Show all variables
            for (Component panel : variableComponents.values()) {
                panel.setVisible(true);
            }
            return;
        }
        
        String lowerFilterText = filterText.toLowerCase();
        boolean hasVisibleComponents = false;
        
        // Filter each variable component
        for (EnvVariable variable : variables) {
            String name = variable.getName().toLowerCase();
            String value = variable.getValue().toLowerCase();
            JPanel panel = (JPanel) variableComponents.get(variable.getName());
            
            if (panel != null) {
                boolean visible = name.contains(lowerFilterText) || value.contains(lowerFilterText);
                panel.setVisible(visible);
                if (visible) {
                    hasVisibleComponents = true;
                }
            }
        }
        
        // Update panel visibility based on whether any variables are visible
        setVisible(hasVisibleComponents);
    }

    public void applyFilter(String filterText) {
        currentFilter = filterText;
        if(filterText.length() > 1) {
            filterVariables(filterText);
        }
    }
    
    /**
     * Display a dialog to edit an environment variable
     * @param variable The variable to edit
     */
    private void showEditDialog(EnvVariable variable) {
        JTextField keyField = new JTextField(variable.getName());
        JTextField valueField = new JTextField(variable.getValue());
        
        if (variable.isSecret()) {
            // For secret variables, show a masked field
            valueField = new JPasswordField(variable.getValue());
        }
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Variable Name:"));
        panel.add(keyField);
        panel.add(new JLabel("Variable Value:"));
        panel.add(valueField);
        
        int result = JOptionPane.showConfirmDialog(
            this, 
            panel, 
            "Edit Environment Variable", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String newName = keyField.getText().trim();
            String newValue = valueField.getText();
            
            // Only proceed if name is not empty
            if (!newName.isEmpty()) {
                if (!newName.equals(variable.getName())) {
                    // Name has changed, we need to remove the old variable and add a new one
                    parentTab.removeEnvironmentVariable(variable.getName());
                    parentTab.addEnvironmentVariable(newName, newValue);
                    statusUpdater.accept("Updated variable: " + newName);
                } else {
                    // Just update the value
                    parentTab.updateEnvironmentVariable(newName, newValue);
                    statusUpdater.accept("Updated variable value: " + newName);
                }
            }
        }
    }
    
    /**
     * Display a confirmation dialog before deleting an environment variable
     * @param variable The variable to delete
     */
    private void showDeleteConfirmation(EnvVariable variable) {
        int result = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to delete the variable " + variable.getName() + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            parentTab.removeEnvironmentVariable(variable.getName());
            statusUpdater.accept("Deleted variable: " + variable.getName());
        }
    }
    
    /**
     * Add a new environment variable button and functionality
     */
    public void addAddVariableButton() {
        JButton addButton = new JButton("+ Add Variable");
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(e -> showAddDialog());
        
        // Add button at the bottom of the variables list
        variablesPanel.add(addButton);
    }
    
    /**
     * Display a dialog to add a new environment variable
     */
    private void showAddDialog() {
        JTextField keyField = new JTextField();
        JTextField valueField = new JTextField();
        JCheckBox secretCheckBox = new JCheckBox("Secret value (mask in UI)");
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Variable Name:"));
        panel.add(keyField);
        panel.add(new JLabel("Variable Value:"));
        panel.add(valueField);
        panel.add(secretCheckBox);
        
        int result = JOptionPane.showConfirmDialog(
            this, 
            panel, 
            "Add Environment Variable", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String name = keyField.getText().trim();
            String value = valueField.getText();
            
            // Only proceed if name is not empty
            if (!name.isEmpty()) {
                parentTab.addEnvironmentVariable(name, value);
                statusUpdater.accept("Added new variable: " + name);
            }
        }
    }

    /**
     * Refresh the display with updated variables
     * @param updatedVariables The updated list of variables
     */
    public void refreshVariables(List<EnvVariable> updatedVariables) {
        // Clear current variables and components
        variables = updatedVariables;
        variableComponents.clear();
        variablesPanel.removeAll();
        
        // Rebuild the variable controls
        for (EnvVariable variable : variables) {
            JPanel varPanel = createControlForVariable(variable);
            variablesPanel.add(varPanel);
        }
        
        // Re-apply any current filter
        if (currentFilter != null && !currentFilter.isEmpty()) {
            applyFilter(currentFilter);
        }
        
        variablesPanel.revalidate();
        variablesPanel.repaint();
    }
}
