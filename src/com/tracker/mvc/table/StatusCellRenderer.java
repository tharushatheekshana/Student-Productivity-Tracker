package com.tracker.mvc.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class StatusCellRenderer extends DefaultTableCellRenderer {

    public StatusCellRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null) {
            String status = value.toString();
            setFont(table.getFont().deriveFont(Font.BOLD));
            
            // Logic for Overdue column ("Yes"/"No")
            if (status.equalsIgnoreCase("No")) { 
                setForeground(new Color(0, 153, 51)); // Green for Not Overdue
            } else if (status.equalsIgnoreCase("Yes")) { 
                setForeground(new Color(220, 53, 69)); // Red for Overdue
            } 
            // Logic for other status if needed
            else {
                 if (isSelected) {
                    setForeground(table.getSelectionForeground());
                } else {
                    setForeground(table.getForeground());
                }
            }
        }
        return com;
    }
}
