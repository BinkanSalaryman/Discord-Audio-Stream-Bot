package net.gui.renderer;

import javax.swing.*;
import java.awt.*;

public abstract class StandardListCellRenderer<T> extends JLabel implements ListCellRenderer<T> {
    public StandardListCellRenderer() {
        // Leave a 10-pixel separator between the icon and label.

        setIconTextGap(10);

        // Swing labels default to being transparent; the container's color
        // shows through. To change a Swing label's background color, you must
        // first make the label opaque (by passing true to setOpaque()). Later,
        // you invoke setBackground(), passing the new color as the argument.

        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        apply(value);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setFont(list.getFont());
        setEnabled(list.isEnabled());

        return this;
    }

    protected abstract void apply(T value);
}
