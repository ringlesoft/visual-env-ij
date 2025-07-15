package com.ringlesoft.visualenv.toolWindow;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;
import com.ringlesoft.visualenv.services.EnvVariableService;

import javax.swing.*;
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
    private final List<EnvVariable> variables;
    private final EnvVariableService envVariableService;
    private final Consumer<String> statusUpdater;
    private final Map<String, Component> variableComponents = new HashMap<>();
    private final JPanel variablesPanel;
    private final JLabel titleLabel;
    private boolean expanded = true;
    
    public EnvGroupPanel(String groupName, List<EnvVariable> variables, EnvVariableService envVariableService, Consumer<String> statusUpdater) {
        this.groupName = groupName;
        this.variables = variables;
        this.envVariableService = envVariableService;
        this.statusUpdater = statusUpdater;
        
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
        for (EnvVariable variable : variables) {
            JPanel varPanel = createControlForVariable(variable);
            variablesPanel.add(varPanel);
        }
        
        // Add to scroll pane to handle overflow
        JScrollPane scrollPane = new JScrollPane(variablesPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createControlForVariable(EnvVariable variable) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(JBUI.Borders.emptyBottom(5));
        
        // Variable name
        JLabel nameLabel = new JBLabel(variable.getName() + ":");
        nameLabel.setPreferredSize(new Dimension(150, nameLabel.getPreferredSize().height));
        nameLabel.setToolTipText(getDescriptionForVariable(variable));
        
        panel.add(nameLabel, BorderLayout.WEST);
        
        // Variable control depends on type
        Component control = createControlByType(variable);
        panel.add(control, BorderLayout.CENTER);
        
        // Store reference to the component for filtering
        variableComponents.put(variable.getName(), panel);
        
        return panel;
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
     * Filter variables in this group by the given filter text.
     * Hides variables that don't match the filter.
     * 
     * @param filterText the text to filter variables by (lowercase)
     */
    public void filterVariables(String filterText) {
        int visibleCount = 0;
        
        for (EnvVariable variable : variables) {
            Component component = variableComponents.get(variable.getName());
            if (component != null) {
                boolean matches = variable.getName().toLowerCase().contains(filterText) || 
                                 (variable.getValue() != null && variable.getValue().toLowerCase().contains(filterText));
                
                component.setVisible(matches);
                if (matches) {
                    visibleCount++;
                }
            }
        }
        
        // Update title to show filtered count
        if (filterText.isEmpty()) {
            titleLabel.setText(groupName + " (" + variables.size() + ")");
        } else {
            titleLabel.setText(groupName + " (" + visibleCount + "/" + variables.size() + ")");
        }
        
        // Hide the group entirely if no variables match
        setVisible(visibleCount > 0);
    }

    public void applyFilter(String filterText) {
        if(filterText.length() > 1) {
//            filterVariables(filterText);
        }
    }
}
