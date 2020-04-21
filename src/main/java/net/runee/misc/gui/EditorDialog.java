package net.runee.misc.gui;

import com.jgoodies.forms.builder.FormBuilder;
import net.runee.misc.Utils;
import net.runee.misc.gui.DialogResult;
import net.runee.misc.gui.SpecBuilder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public abstract class EditorDialog<T extends JComponent> extends JDialog {
    private DialogResult dialogResult = DialogResult.cancel;
    private T editor;

    protected JButton okButton;
    protected JButton cancelButton;

    public DialogResult getDialogResult() {
        return dialogResult;
    }

    public EditorDialog(Frame owner, T editor) {
        super(owner, true);
        this.editor = editor;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        initButtons();
    }

    protected void initButtons() {
        okButton = new JButton("Ok");
        okButton.addActionListener(e -> close(DialogResult.ok));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> close(DialogResult.cancel));
    }

    protected JButton[] getButtons() {
        return new JButton[]{okButton, cancelButton};
    }

    private void layoutComponents() {
        setContentPane(new BorderPanel(new ContentControlPanel(
                editor,
                Utils.buildFlowPanel(getButtons()),
                true
        )));
    }

    protected T getEditor() {
        return editor;
    }

    public void close(DialogResult dialogResult) {
        this.dialogResult = dialogResult;
        dispose();
    }
}
