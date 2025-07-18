package com.ringlesoft.visualenv.toolWindow;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomDialogWindow extends JDialog {
    private JPanel contentPanel;
    private JPanel actionsPanel;
    private GridBagConstraints contentGbc;
    
    public CustomDialogWindow(String title) {
        super();
        initializeUI(title);
    }

    private void initializeUI(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(true);
        setLayout(new BorderLayout());

        // Main form panel with proper spacing
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(JBUI.Borders.empty(20));
        
        // Initialize GridBagConstraints for content
        contentGbc = new GridBagConstraints();
        contentGbc.gridx = 0;
        contentGbc.gridy = 0;
        contentGbc.anchor = GridBagConstraints.WEST;
        contentGbc.fill = GridBagConstraints.HORIZONTAL;
        contentGbc.weightx = 1.0;

        // Actions panel with proper button layout
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setBorder(JBUI.Borders.empty(10, 15, 15, 15));
        
        add(contentPanel, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);
    }

    public void addContent(JComponent component) {
        addContent(component, JBUI.insetsBottom(15));
    }
    
    public void addContent(JComponent component, Insets insets) {
        contentGbc.insets = insets;
        contentPanel.add(component, contentGbc);
        contentGbc.gridy++;
    }

    public void addActions(JComponent component) {
        actionsPanel.add(component);
    }

    public void addButton(String title, ActionListener actionListener) {
        JButton button = new JButton(title);
        button.addActionListener(actionListener);
        actionsPanel.add(button);
    }
    
    public void showDialog() {
        showDialog(350, -1, true);
    }
    
    public void showDialog(int width, int height) {
        showDialog(width, height, true);
    }

    private void showDialog(int width, int height, boolean centerOnScreen) {
        pack();
        if (height > 0) {
            setSize(width, height);
        } else {
            setSize(width, getHeight());
        }
        if (centerOnScreen) {
            setLocationRelativeTo(null); // Center on screen
        }
        setVisible(true);
    }
    
    public void focusFirstComponent() {
        Component[] components = contentPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JTextField) {
                component.requestFocus();
                if (component instanceof JTextField) {
                    ((JTextField) component).selectAll();
                }
                break;
            }
        }
    }
}
