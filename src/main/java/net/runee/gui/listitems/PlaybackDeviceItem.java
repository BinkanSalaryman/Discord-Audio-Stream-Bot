package net.runee.gui.listitems;

public class PlaybackDeviceItem {
    private final String name;
    private final int device;

    public PlaybackDeviceItem(String name, int device) {
        this.name = name;
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public int getDevice() {
        return device;
    }
}
