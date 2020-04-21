package net.runee.gui.renderer;

import net.runee.gui.listitems.PlaybackDeviceItem;

public class PlaybackDeviceListCellRenderer extends net.gui.renderer.StandardListCellRenderer<PlaybackDeviceItem> {
    @Override
    protected void apply(PlaybackDeviceItem value) {
        setText(value != null ? value.getName() : "(Default playback device)");
    }
}
