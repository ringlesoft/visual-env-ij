package com.ringlesoft.visualenv.ui;

import com.intellij.ui.JBColor;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.BorderFactory;
import com.intellij.util.ui.JBUI;

import java.awt.Color;

/**
 * Theme definition for Visual Env plugin UI components
 * Centralizes color definitions and UI styles for consistent appearance and easier maintenance
 * Supports both light and dark IDE themes using JBColor
 */
public class VisualEnvTheme {
    
    // Primary colors
    public static final JBColor PRIMARY = new JBColor(new Color(76, 139, 202), new Color(104, 159, 207));
    public static final JBColor PRIMARY_LIGHT = new JBColor(new Color(148, 187, 224), new Color(88, 135, 176));
    public static final JBColor PRIMARY_DARK = new JBColor(new Color(44, 95, 145), new Color(55, 100, 150));
    
    // Secondary colors
    public static final JBColor ACCENT = new JBColor(new Color(131, 193, 103), new Color(105, 181, 85));
    
    // Functional colors
    public static final JBColor SUCCESS = new JBColor(new Color(87, 174, 128), new Color(77, 153, 105));
    public static final JBColor WARNING = new JBColor(new Color(232, 192, 88), new Color(196, 160, 55));
    public static final JBColor ERROR = new JBColor(new Color(235, 95, 85), new Color(209, 83, 73));
    
    // Text colors
    public static final JBColor TEXT_PRIMARY = new JBColor(new Color(50, 50, 50), new Color(220, 220, 220));
    public static final JBColor TEXT_SECONDARY = new JBColor(new Color(120, 120, 120), new Color(170, 170, 170));
    public static final JBColor TEXT_DISABLED = new JBColor(new Color(170, 170, 170), new Color(120, 120, 120));
    
    // Background colors
    public static final JBColor BACKGROUND_DEFAULT = (JBColor) JBColor.background();
    public static final JBColor BACKGROUND_PANEL = new JBColor(new Color(245, 245, 245), new Color(50, 50, 50));
    public static final JBColor BACKGROUND_HIGHLIGHT = new JBColor(new Color(240, 245, 250), new Color(45, 55, 65));
    
    // Border colors
    public static final JBColor BORDER = new JBColor(new Color(210, 210, 210), new Color(80, 80, 80));
    public static final JBColor BORDER_LIGHT = new JBColor(new Color(230, 230, 230), new Color(60, 60, 60));
    
    // Common borders
    public static Border PANEL_BORDER = BorderFactory.createLineBorder(BORDER);
    public static Border PANEL_BORDER_WITH_PADDING = new CompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            JBUI.Borders.empty(5)
    );
    
    // Group panel borders
    public static Border GROUP_HEADER_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            JBUI.Borders.empty(5)
    );
    
    // Text field borders
    public static Border TEXT_FIELD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            JBUI.Borders.empty(2, 4)
    );
    
    /**
     * Get a border with specified insets
     * 
     * @param top Top inset
     * @param left Left inset
     * @param bottom Bottom inset
     * @param right Right inset
     * @return Border with specified insets
     */
    public static Border getBorderWithInsets(int top, int left, int bottom, int right) {
        return new CompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                JBUI.Borders.empty(top, left, bottom, right)
        );
    }
    
    /**
     * Get panel background color based on the highlight state
     * 
     * @param highlighted Whether the panel should be highlighted
     * @return The appropriate background color
     */
    public static JBColor getPanelBackground(boolean highlighted) {
        return highlighted ? BACKGROUND_HIGHLIGHT : BACKGROUND_PANEL;
    }
    
    /**
     * Get text color based on the enabled state
     * 
     * @param enabled Whether the text is enabled
     * @return The appropriate text color
     */
    public static JBColor getTextColor(boolean enabled) {
        return enabled ? TEXT_PRIMARY : TEXT_DISABLED;
    }
}
