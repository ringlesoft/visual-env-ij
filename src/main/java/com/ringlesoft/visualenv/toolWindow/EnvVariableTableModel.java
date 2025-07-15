package com.ringlesoft.visualenv.toolWindow;

import com.ringlesoft.visualenv.model.EnvVariable;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for displaying environment variables.
 */
public class EnvVariableTableModel extends AbstractTableModel {
    private final List<EnvVariable> variables = new ArrayList<>();
    private final String[] columnNames = {"Name", "Value", "Source"};

    @Override
    public int getRowCount() {
        return variables.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EnvVariable variable = variables.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return variable.getName();
            case 1:
                return variable.getValue();
            case 2:
                return variable.getSource();
            default:
                return null;
        }
    }

    /**
     * Updates the table data with a new list of variables.
     *
     * @param newVariables New variables to display
     */
    public void setVariables(List<EnvVariable> newVariables) {
        variables.clear();
        if (newVariables != null) {
            variables.addAll(newVariables);
        }
        fireTableDataChanged();
    }

    /**
     * Gets the environment variable at the specified row.
     *
     * @param rowIndex Row index
     * @return The environment variable
     */
    public EnvVariable getVariableAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < variables.size()) {
            return variables.get(rowIndex);
        }
        return null;
    }
}
