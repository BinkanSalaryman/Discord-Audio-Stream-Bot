package net.runee.misc.gui;

import com.jgoodies.forms.builder.FormBuilder;

import javax.swing.*;

public class BorderPanel extends JPanel {
    public static JPanel layout(JPanel panel, JComponent component) {
        FormBuilder builder = FormBuilder
                .create()
                .columns("f:p:g")
                .rows("f:p:g");
        if(panel != null) {
            builder.panel(panel);
        }
        return builder
                .border(BorderFactory.createEmptyBorder(5, 5, 5, 5))
                .add(component).xy(1, 1)
                .build();
    }

    public BorderPanel(JComponent component) {
        layout(this, component);
    }
}
