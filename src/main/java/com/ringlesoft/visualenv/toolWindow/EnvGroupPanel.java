package com.ringlesoft.visualenv.toolWindow;

import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.model.EnvVariableRegistry;
import com.ringlesoft.visualenv.services.EnvVariableService;
import com.ringlesoft.visualenv.ui.VisualEnvTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private List<EnvVariable> variables;
    private final EnvVariableService envVariableService;
    private final Consumer<String> statusUpdater;
    private final Map<String, Component> variableComponents = new HashMap<>();
    private final JPanel variablesPanel;
    private final JLabel titleLabel;
    private boolean expanded = true;
    private String currentFilter;
    
    // Debounce related fields
    private final Map<String, Timer> debounceTimers = new HashMap<>();
    private static final int DEBOUNCE_DELAY = 500; // milliseconds

    public EnvGroupPanel(String groupName, List<EnvVariable> variables, EnvVariableService envVariableService, Consumer<String> statusUpdater, EnvEditorTab parentTab) {
        this.variables = variables;
        this.envVariableService = envVariableService;
        this.statusUpdater = statusUpdater;

        // Setup panel
        setLayout(new BorderLayout());
        setBorder(VisualEnvTheme.PANEL_BORDER);
        
        // Set alignment and sizing for proper scrolling behavior
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        
        // Create the title panel with a chevron for expand/collapse
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(VisualEnvTheme.GROUP_HEADER_BORDER);
        
        // Add expand/collapse icon
        JLabel expandIcon = new JLabel("-");
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
                expandIcon.setText(expanded ? "-" : "+");
                variablesPanel.setVisible(expanded);
                revalidate();
                repaint();
            }
        });
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create panel for variables
        variablesPanel = new JPanel();
        variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));
        variablesPanel.setBorder(VisualEnvTheme.VARIABLES_PANEL_BORDER);
        
        // Add each variable control
        for (EnvVariable variable : variables) {
            JPanel varPanel = createControlForVariable(variable);
            variablesPanel.add(varPanel);
        }
        add(variablesPanel, BorderLayout.CENTER);
    }
    
    private JPanel createControlForVariable(EnvVariable variable) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(VisualEnvTheme.VARIABLE_PANEL_BORDER);
        
        // Create variable name label
        JLabel nameLabel = new JLabel(variable.getName() + ":");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        nameLabel.setBorder(VisualEnvTheme.VARIABLE_NAME_BORDER);
        nameLabel.setToolTipText(getDescriptionForVariable(variable));
        panel.add(nameLabel, BorderLayout.WEST);
        
        // Create variable value component based on type
        Component valueComponent = createControlByType(variable);
        panel.add(valueComponent, BorderLayout.CENTER);

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
            textField = new JPasswordField(variable.getValue());
        } else {
            textField = new JTextField(variable.getValue());
        }
        
        // Set preferred size to maintain consistent height
        Dimension preferredSize = textField.getPreferredSize();
        preferredSize.height = 28;  // Fixed height for text fields
        textField.setPreferredSize(preferredSize);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height));
        
        textField.addActionListener(e -> updateVariable(variable.getName(), textField.getText()));
        
        // Add document listener for debounced saving
        addDebounceListener(textField, variable.getName());
        
        return textField;
    }
    
    private Component createBooleanControl(EnvVariable variable) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected("true".equalsIgnoreCase(variable.getValue()) || 
                             "1".equals(variable.getValue()) || 
                             "yes".equalsIgnoreCase(variable.getValue()));
        
        // Set preferred size to maintain consistent height
        Dimension preferredSize = checkBox.getPreferredSize();
        checkBox.setPreferredSize(preferredSize);
        checkBox.setMaximumSize(preferredSize);
        
        checkBox.addItemListener(e -> {
            boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;
            updateVariable(variable.getName(), isSelected ? "true" : "false");
        });
        
        return checkBox;
    }
    
    private Component createDropdownControl(EnvVariable variable, List<String> possibleValues) {
        JComboBox<String> comboBox = new JComboBox<>();
        
        for (String value : possibleValues) {
            comboBox.addItem(value);
        }
        
        // Set current value
        comboBox.setSelectedItem(variable.getValue());
        
        // Set preferred size to maintain consistent height
        Dimension preferredSize = comboBox.getPreferredSize();
        preferredSize.height = 28;  // Fixed height for dropdown
        comboBox.setPreferredSize(preferredSize);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height));
        
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedValue = (String) comboBox.getSelectedItem();
                updateVariable(variable.getName(), selectedValue);
            }
        });
        
        return comboBox;
    }
    
    private Component createIntegerControl(EnvVariable variable) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                Integer.parseInt(variable.getValue().isEmpty() ? "0" : variable.getValue()),
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                1
        ));
        
        // Set preferred size to maintain consistent height
        Dimension preferredSize = spinner.getPreferredSize();
        preferredSize.height = 28;  // Fixed height for spinner
        spinner.setPreferredSize(preferredSize);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height));
        
        spinner.addChangeListener(e -> updateVariable(variable.getName(), spinner.getValue().toString()));
        
        return spinner;
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
     * Apply filtering to show only variables matching the filter
     */
    public void applyFilter(String filterText) {
        this.currentFilter = filterText;
        updateVariablesPanel();
    }

    /**
     * Updates the variables panel based on the current filter
     */
    private void updateVariablesPanel() {
        variablesPanel.removeAll();
        int visibleCount = 0;
        
        for (int i = 0; i < variables.size(); i++) {
            EnvVariable variable = variables.get(i);
            
            boolean shouldShow = currentFilter == null || currentFilter.isEmpty() ||
                    variable.getName().toLowerCase().contains(currentFilter) ||
                    variable.getValue().toLowerCase().contains(currentFilter) ;
            
            if (shouldShow) {
                JPanel varPanel = createControlForVariable(variable);
                variablesPanel.add(varPanel);
                visibleCount++;
                
                // Add a separator between variables
                if (visibleCount < countVisibleVariables()) {
                    variablesPanel.add(Box.createRigidArea(new Dimension(0, 1)));
                }
            }
        }
        
        // Update visibility based on expanded state and if there are visible variables
        updatePanelVisibility(visibleCount > 0);
        
        // Update the maximum size based on preferred size after content changes
        updateMaximumSize();
        
        // Refresh the UI
        variablesPanel.revalidate();
        variablesPanel.repaint();
    }
    
    /**
     * Updates the maximum size based on current content
     */
    private void updateMaximumSize() {
        revalidate();
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }
    
    /**
     * Count how many variables would be visible with current filter
     */
    private int countVisibleVariables() {
        if (currentFilter == null || currentFilter.isEmpty()) {
            return variables.size();
        }
        
        return (int) variables.stream()
            .filter(v -> v.getName().toLowerCase().contains(currentFilter) ||
                    v.getValue().toLowerCase().contains(currentFilter))
            .count();
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
    
    /**
     * Adds a debounced document listener to text fields
     * This ensures that changes are saved after the user stops typing, rather than on every keystroke
     * 
     * @param textField The text field to monitor
     * @param variableName The name of the variable to update
     */
    private void addDebounceListener(JTextField textField, String variableName) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                debounceUpdate(variableName, textField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounceUpdate(variableName, textField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounceUpdate(variableName, textField.getText());
            }
        });
    }
    
    /**
     * Handles the debounced update for a variable value
     * Cancels any existing timer and creates a new one
     * 
     * @param variableName The name of the variable to update
     * @param value The new value to set
     */
    private void debounceUpdate(String variableName, String value) {
        // Cancel any existing timer for this variable
        Timer existingTimer = debounceTimers.get(variableName);
        if (existingTimer != null) {
            existingTimer.stop();
        }
        
        // Create a new timer
        Timer timer = new Timer(DEBOUNCE_DELAY, e -> {
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                updateVariable(variableName, value);
            }, com.intellij.openapi.application.ModalityState.defaultModalityState());
        });
        timer.setRepeats(false);
        timer.start();
        
        // Store the timer
        debounceTimers.put(variableName, timer);
    }
    
    private void updatePanelVisibility(boolean visible) {
        variablesPanel.setVisible(visible && expanded);
        setVisible(visible && expanded);
    }
}
