package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import com.ringlesoft.visualenv.VisualEnvBundle;
import com.ringlesoft.visualenv.model.EnvVariable;
import com.ringlesoft.visualenv.services.EnvVariableService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Tool window factory for Visual Env plugin.
 */
public class VisualEnvToolWindowFactory implements ToolWindowFactory {
    private static final Logger LOG = Logger.getInstance(VisualEnvToolWindowFactory.class);

    public VisualEnvToolWindowFactory() {
        LOG.info("VisualEnvToolWindowFactory initialized");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        VisualEnvToolWindow visualEnvToolWindow = new VisualEnvToolWindow(toolWindow, project);
        Content content = ContentFactory.getInstance().createContent(
                visualEnvToolWindow.getContent(), 
                VisualEnvBundle.message("visualenv.toolwindow.title"), 
                false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    /**
     * Inner class representing the tool window UI.
     */
    private static class VisualEnvToolWindow {
        private final Project project;
        private final EnvVariableService envVariableService;
        private final EnvVariableTableModel systemEnvModel = new EnvVariableTableModel();
        private final EnvVariableTableModel fileEnvModel = new EnvVariableTableModel();
        private JLabel fileStatusLabel;
        private JLabel systemStatusLabel;
        private JBTable systemEnvTable;

        public VisualEnvToolWindow(ToolWindow toolWindow, Project project) {
            this.project = project;
            this.envVariableService = project.getService(EnvVariableService.class);
        }

        public JComponent getContent() {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            // Create tabbed pane for different views
            JBTabbedPane tabbedPane = new JBTabbedPane();
            
            // System Environment Variables tab
            JPanel systemEnvPanel = createSystemEnvPanel();
            tabbedPane.addTab(VisualEnvBundle.message("visualenv.systemenv.tab"), systemEnvPanel);
            
            // Project Environment Files tab
            JPanel fileEnvPanel = createFileEnvPanel();
            tabbedPane.addTab(VisualEnvBundle.message("visualenv.fileenv.tab"), fileEnvPanel);
            
            mainPanel.add(tabbedPane, BorderLayout.CENTER);
            
            // Initialize with system environment variables
            updateSystemEnvironmentVariables();
            
            return mainPanel;
        }
        
        private JPanel createSystemEnvPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(new EmptyBorder(10, 0, 10, 0));
            
            // Add filter field
            JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
            JLabel filterLabel = new JBLabel("Filter: ");
            JTextField filterField = new JTextField(20);
            filterPanel.add(filterLabel, BorderLayout.WEST);
            filterPanel.add(filterField, BorderLayout.CENTER);
            panel.add(filterPanel, BorderLayout.NORTH);
            
            // Create table for system environment variables
            systemEnvTable = new JBTable(systemEnvModel);
            systemEnvTable.setRowHeight(24);
            
            // Add sorting and filtering
            TableRowSorter<EnvVariableTableModel> sorter = new TableRowSorter<>(systemEnvModel);
            systemEnvTable.setRowSorter(sorter);
            
            filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    filterChanged();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    filterChanged();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    filterChanged();
                }
                
                private void filterChanged() {
                    String filter = filterField.getText();
                    if (filter.trim().isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter));
                    }
                }
            });
            
            // Add table to a scroll pane
            JBScrollPane scrollPane = new JBScrollPane(systemEnvTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add refresh button
            JButton refreshButton = new JButton(VisualEnvBundle.message("visualenv.refresh"));
            refreshButton.addActionListener(e -> updateSystemEnvironmentVariables());
            
            // Add status label
            systemStatusLabel = new JBLabel();
            updateSystemVariablesCount();
            
            // Add button panel
            JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));
            buttonPanel.add(refreshButton, BorderLayout.EAST);
            buttonPanel.add(systemStatusLabel, BorderLayout.WEST);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            return panel;
        }
        
        private JPanel createFileEnvPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(new EmptyBorder(10, 0, 10, 0));
            
            // Add filter field
            JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
            JLabel filterLabel = new JBLabel("Filter: ");
            JTextField filterField = new JTextField(20);
            filterPanel.add(filterLabel, BorderLayout.WEST);
            filterPanel.add(filterField, BorderLayout.CENTER);
            panel.add(filterPanel, BorderLayout.NORTH);
            
            // Create table for file environment variables
            JBTable fileEnvTable = new JBTable(fileEnvModel);
            fileEnvTable.setRowHeight(24);
            
            // Add sorting and filtering
            TableRowSorter<EnvVariableTableModel> sorter = new TableRowSorter<>(fileEnvModel);
            fileEnvTable.setRowSorter(sorter);
            
            filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    filterChanged();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    filterChanged();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    filterChanged();
                }
                
                private void filterChanged() {
                    String filter = filterField.getText();
                    if (filter.trim().isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter));
                    }
                }
            });
            
            // Add table to a scroll pane
            JBScrollPane scrollPane = new JBScrollPane(fileEnvTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add buttons for file operations
            JButton openFileButton = new JButton(VisualEnvBundle.message("visualenv.open.file"));
            openFileButton.addActionListener(e -> chooseAndParseEnvFile());
            
            // Add status label
            fileStatusLabel = new JBLabel(VisualEnvBundle.message("visualenv.no.variables"));
            
            // Add button panel
            JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));
            buttonPanel.add(openFileButton, BorderLayout.EAST);
            buttonPanel.add(fileStatusLabel, BorderLayout.WEST);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            return panel;
        }
        
        private void updateSystemEnvironmentVariables() {
            List<EnvVariable> systemVariables = envVariableService.getSystemEnvVariables();
            systemEnvModel.setVariables(systemVariables);
            updateSystemVariablesCount();
        }
        
        private void updateSystemVariablesCount() {
            if (systemStatusLabel != null) {
                systemStatusLabel.setText(systemEnvModel.getRowCount() + " environment variables");
            }
        }
        
        private void chooseAndParseEnvFile() {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle("Select .env File")
                    .withDescription("Choose a .env file to parse")
                    .withFileFilter(file -> file.getName().contains(".env") || file.getName().endsWith(".properties"));
            
            VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, null);
            if (files.length > 0) {
                List<EnvVariable> fileVariables = envVariableService.parseEnvFile(files[0]);
                fileEnvModel.setVariables(fileVariables);
                
                if (fileStatusLabel != null) {
                    if (fileVariables.isEmpty()) {
                        fileStatusLabel.setText(VisualEnvBundle.message("visualenv.no.variables"));
                    } else {
                        fileStatusLabel.setText(fileVariables.size() + " variables from " + files[0].getName());
                    }
                }
            }
        }
    }
}
