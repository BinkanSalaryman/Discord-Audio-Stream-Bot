package net.runee.gui.listitems;

public class RecordingDeviceItem {
    private final String name;
    private final int device;

    public RecordingDeviceItem(String name, int device) {
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
