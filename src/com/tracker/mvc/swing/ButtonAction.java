package com.tracker.mvc.swing;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author RAVEN
 */
public class ButtonAction extends JButton {

    public ButtonAction() {

        putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:15;"
                + "borderWidth:0;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "margin:5,20,5,20;"
                + "background:$Panel.background");
    }
}
