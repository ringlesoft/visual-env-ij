package com.ringlesoft.visualenv.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.model.EnvVariableDefinition;
import com.ringlesoft.visualenv.services.EnvFileService;
import com.ringlesoft.visualenv.services.ProjectService;
import com.ringlesoft.visualenv.ui.VisualEnvTheme;
import com.ringlesoft.visualenv.utils.CommandRunner;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.codehaus.plexus.util.StringUtils.capitalizeFirstLetter;

/**
 * Panel that displays environment variables grouped by category
 */
public class EnvGroupPanel extends JPanel {

    private List<EnvVariable> variables;
    private final EnvFileService envFileService;
    private final ProjectService projectService;
    private final Consumer<String> statusUpdater;
    private final JPanel variablesPanel;
    private boolean expanded = true;
    private String currentFilter;
    
    // Debounce related fields
    private final Map<String, Timer> debounceTimers = new HashMap<>();
    private static final int DEBOUNCE_DELAY = 1500; // milliseconds

    public EnvGroupPanel(String groupName, List<EnvVariable> variables, EnvFileService envFileService, ProjectService projectService, Consumer<String> statusUpdater, EnvEditorTab parentTab) {
        this.variables = variables;
        this.envFileService = envFileService;
        this.projectService = projectService;
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
        expandIcon.setForeground(VisualEnvTheme.TEXT_SECONDARY);
        headerPanel.add(expandIcon, BorderLayout.WEST);
        
        // Group name and count
        JLabel titleLabel = new JLabel(capitalizeFirstLetter(groupName));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setForeground(VisualEnvTheme.TEXT_SECONDARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Add count badge to the right
        JLabel countBadge = new JLabel(variables.size() + " ");
        countBadge.setForeground(VisualEnvTheme.TEXT_SECONDARY);
        // reduce font size by 1
        countBadge.setFont(countBadge.getFont().deriveFont(Font.PLAIN, countBadge.getFont().getSize() - 2));
        countBadge.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        headerPanel.add(countBadge, BorderLayout.EAST);
        
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
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, nameLabel.getFont().getSize() - 1));
        nameLabel.setBorder(VisualEnvTheme.VARIABLE_NAME_BORDER);
        nameLabel.setToolTipText(getDescriptionForVariable(variable));

        // Add right-click context menu
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Copy Variable Name");
        copyItem.setIcon(AllIcons.Actions.Copy);
        copyItem.setHorizontalAlignment(SwingConstants.LEFT);
        copyItem.setPreferredSize(new Dimension(180, 30));
        copyItem.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(variable.getName()), null);
            statusUpdater.accept("Variable name copied to clipboard");
        });
        contextMenu.add(copyItem);

        JMenuItem renameItem = new JMenuItem("Rename Variable");
        renameItem.setIcon(AllIcons.Actions.Edit);
        renameItem.setHorizontalAlignment(SwingConstants.LEFT);
        renameItem.setPreferredSize(new Dimension(180, 30));
        renameItem.addActionListener(e -> showRenameDialog(variable));
        contextMenu.add(renameItem);

        JMenuItem deleteItem = new JMenuItem("Delete Variable");
        deleteItem.setIcon(AllIcons.Actions.GC);
        deleteItem.setHorizontalAlignment(SwingConstants.LEFT);
        deleteItem.setPreferredSize(new Dimension(180, 30));
        deleteItem.addActionListener(e -> {
            if(envFileService.deleteEnvVariable(variable.getName())){
                // Remove the variable from the list
            } else {
                // failed
            }
        });

        // Hover Effect
        addHoverEffect(copyItem);
        addHoverEffect(renameItem);
        addHoverEffect(deleteItem);

        contextMenu.add(deleteItem);

        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    contextMenu.show(nameLabel, e.getX(), e.getY());
                }
            }
        });
        panel.add(nameLabel, BorderLayout.WEST);

        // Create variable value component based on type
        Component valueComponent = createControlByType(variable);
        panel.add(valueComponent, BorderLayout.CENTER);

        return panel;
    }
    
    private Component createControlByType(EnvVariable variable) {
        EnvVariableDefinition definition = envFileService.getActiveProfile().getDefinition(variable.getName());
        
        if (definition != null) {
            return switch (definition.getType()) {
                case BOOLEAN -> createBooleanControl(variable);
                case DROPDOWN -> createDropdownControl(variable, definition.getPossibleValues());
                case INTEGER -> createIntegerControl(variable);
                case GENERATED -> createGeneratorControl(variable);
                default -> createTextControl(variable, variable.isSecret());
            };
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
        JComboBox<String> comboBox = new ComboBox<>();
        
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

        if(variable.hasInterpolation()){ // If the variable has interpolation just make it a text field
            return createTextControl(variable, variable.isSecret());
        }

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

    private Component createGeneratorControl(EnvVariable variable) {
        // For now this is bypassed. TODO: Work on this for the next version
        if(true){
            return createTextControl(variable, variable.isSecret());
        }
        EnvVariableDefinition definition = envFileService.getActiveProfile().getDefinition(variable.getName());
        if(definition != null && definition.getGeneratorCommand() != null) {
            String command = definition.getGeneratorCommand();
            JButton button = new JButton("Generate");
            button.setToolTipText("Generate " + variable.getName());
            button.addActionListener(e -> {
                try {
                    button.setText("Generating...");
                    CommandRunner commandRunner =  projectService.getCommandRunner();
                    commandRunner.runCommand(command);
                } catch (Exception ex) {
                    // Failed to execute
                } finally {
                    button.setText("Generate");
                }
            });
            // Set preferred size to maintain consistent height
            Dimension preferredSize = button.getPreferredSize();
            preferredSize.height = 28;  // Fixed height for button
            button.setPreferredSize(preferredSize);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height));
            return button;
        }
        return createTextControl(variable, variable.isSecret());
    }
    
    private String getDescriptionForVariable(EnvVariable variable) {
        EnvVariableDefinition definition = envFileService.getActiveProfile().getDefinition(variable.getName());
        return definition != null ? definition.getDescription() : "";
    }
    
    private void updateVariable(String name, String value) {
        boolean success = envFileService.updateEnvVariable(name, value);
        if (success) {
            statusUpdater.accept("Updated " + name + " to " + (isSecretVariable(name) ? "*".repeat(name.length()) : value));
        } else {
            statusUpdater.accept("Failed to update " + name);
        }
    }
    
    private boolean isSecretVariable(String name) {
        EnvVariableDefinition definition = envFileService.getActiveProfile().getDefinition(name);
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

        for (EnvVariable variable : variables) {
            boolean shouldShow = currentFilter == null || currentFilter.isEmpty() ||
                    variable.getName().toLowerCase().contains(currentFilter) ||
                    variable.getValue().toLowerCase().contains(currentFilter);

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
        Timer timer = new Timer(DEBOUNCE_DELAY, e
                -> com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(()
                -> updateVariable(variableName, value), com.intellij.openapi.application.ModalityState.defaultModalityState()));
        timer.setRepeats(false);
        timer.start();
        
        // Store the timer
        debounceTimers.put(variableName, timer);
    }
    
    private void updatePanelVisibility(boolean visible) {
        variablesPanel.setVisible(visible && expanded);
        setVisible(visible && expanded);
    }

    private void showRenameDialog(EnvVariable variable) {
        // Create a modal dialog for adding a new variable
        JDialog renameDialog = new JDialog();
        renameDialog.setTitle("Rename " + variable.getName());
        renameDialog.setModal(true);
        renameDialog.setLayout(new BorderLayout());

        // Main form panel with proper spacing
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(JBUI.Borders.empty(20));
        GridBagConstraints gbc = new GridBagConstraints();

        // Variable Name section
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insetsBottom(5);
        JLabel keyLabel = new JLabel("New Name:");
        formPanel.add(keyLabel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = JBUI.insetsBottom(15);
        JTextField keyField = new JTextField(20);
        keyField.setText(variable.getName());
        formPanel.add(keyField, gbc);

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
        JButton saveButton = new JButton("Save");

        cancelButton.addActionListener(event -> renameDialog.dispose());
        saveButton.addActionListener(event -> {
            String key = keyField.getText();
            messagePane.setText("");
            if (key.isEmpty()) {
                return;
            }
            if (envFileService.renameVariable(variable.getName(), key)) {
                renameDialog.dispose();
            } else {
                messagePane.setText("Failed to rename variable");
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        renameDialog.add(formPanel, BorderLayout.CENTER);
        renameDialog.add(buttonPanel, BorderLayout.SOUTH);

        renameDialog.pack();
        renameDialog.setSize(350, renameDialog.getHeight()); // Slightly wider for better proportions
        renameDialog.setLocationRelativeTo(null); // Center on screen

        // Focus the key field
        keyField.requestFocus();
        keyField.selectAll();
        renameDialog.setVisible(true);
    }


    private void addHoverEffect(JMenuItem item) {
        Color originalBg = item.getBackground();
        Color hoverBg = VisualEnvTheme.BACKGROUND_HIGHLIGHT;

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(hoverBg);
                item.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(originalBg);
                item.setOpaque(false);
            }
        });
    }

}
