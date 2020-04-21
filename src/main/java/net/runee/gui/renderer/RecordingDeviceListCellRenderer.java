package net.runee.gui.renderer;

import net.runee.misc.Utils;
import net.runee.gui.listitems.RecordingDeviceItem;

public class RecordingDeviceListCellRenderer extends net.gui.renderer.StandardListCellRenderer<RecordingDeviceItem> {
    @Override
    protected void apply(RecordingDeviceItem value) {
        setText(value != null ? value.getName() : "(Default recording device)");
    }
}
