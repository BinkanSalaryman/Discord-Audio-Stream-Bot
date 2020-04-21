package net.runee.misc.gui;

import com.jgoodies.forms.builder.FormBuilder;

import javax.swing.*;

public class ContentControlPanel extends JPanel {
    public static JPanel layout(JPanel panel, JComponent content, JComponent control, boolean scrollable) {
        FormBuilder builder = FormBuilder
                .create()
                .columns("f:p:g")
                .rows(SpecBuilder
                        .create()
                        .add("f:p:g")
                        .gapUnrelated().add("c:p")
                        .add("c:p")
                        .build()
                );
        if (panel != null) {
            builder.panel(panel);
        }
        if (scrollable) {
            content = new JScrollPane(content);
        }
        return builder
                .add(content).xy(1, 1)
                .addSeparator("").xy(1, 3)
                .add(control).xy(1, 5)
                .build();
    }

    public ContentControlPanel(JComponent content, JComponent control, boolean scrollable) {
        layout(this, content, control, scrollable);
    }
}
