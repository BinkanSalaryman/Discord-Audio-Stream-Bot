package net.runee.gui.components;

import com.jgoodies.forms.builder.FormBuilder;
import jouvieje.bass.Bass;
import jouvieje.bass.structures.BASS_DEVICEINFO;
import net.dv8tion.jda.api.JDA;
import net.runee.DiscordAudioStreamBot;
import net.runee.gui.renderer.PlaybackDeviceListCellRenderer;
import net.runee.gui.renderer.RecordingDeviceListCellRenderer;
import net.runee.gui.listitems.PlaybackDeviceItem;
import net.runee.misc.Utils;
import net.runee.misc.gui.SpecBuilder;
import net.runee.gui.listitems.RecordingDeviceItem;
import net.runee.model.Config;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class SettingsPanel extends JPanel {
    // general
    private JTextField botToken;
    private JCheckBox clearLogOnStart;

    // audio
    private JButton speakEnabled;
    private JButton listenEnabled;
    private JList<RecordingDeviceItem> recordingDevices;
    private JList<PlaybackDeviceItem> playbackDevices;

    public SettingsPanel() {
        initComponents();
        layoutComponents();
        loadConfig();
    }

    private void initComponents() {
        final DiscordAudioStreamBot bot = DiscordAudioStreamBot.getInstance();

        // general
        botToken = new JTextField();
        Utils.addChangeListener(botToken, e -> {
            DiscordAudioStreamBot.getConfig().botToken = Utils.emptyStringToNull(((JTextField) e.getSource()).getText());
            saveConfig();
        });
        clearLogOnStart = new JCheckBox();
        clearLogOnStart.addActionListener(e -> {
            DiscordAudioStreamBot.getConfig().clearLogOnStart = ((JCheckBox) e.getSource()).isSelected();
            saveConfig();
        });

        // audio
        speakEnabled = new JButton();
        speakEnabled.addActionListener(e -> {
            final Config cfg = DiscordAudioStreamBot.getConfig();
            cfg.speakEnabled = !cfg.speakEnabled;
            bot.setSpeakEnabled(cfg.speakEnabled);
            updateSpeakEnabled();
            saveConfig();
        });
        listenEnabled = new JButton();
        listenEnabled.addActionListener(e -> {
            final Config cfg = DiscordAudioStreamBot.getConfig();
            cfg.listenEnabled = !cfg.listenEnabled;
            bot.setListenEnabled(cfg.listenEnabled);
            updateListenEnabled();
            saveConfig();
        });
        recordingDevices = new JList<>();
        recordingDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordingDevices.setCellRenderer(new RecordingDeviceListCellRenderer());
        recordingDevices.addListSelectionListener(e -> {
            if (recordingDevices.getSelectedIndex() >= 0) {
                RecordingDeviceItem value = recordingDevices.getSelectedValue();
                String recordingDevice = value != null ? value.getName() : null;
                bot.setRecordingDevice(recordingDevice);
                DiscordAudioStreamBot.getConfig().recordingDevice = recordingDevice;
                saveConfig();
            }
        });
        playbackDevices = new JList<>();
        playbackDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playbackDevices.setCellRenderer(new PlaybackDeviceListCellRenderer());
        playbackDevices.addListSelectionListener(e -> {
            if (playbackDevices.getSelectedIndex() >= 0) {
                PlaybackDeviceItem value = playbackDevices.getSelectedValue();
                String playbackDevice = value != null ? value.getName() : null;
                bot.setPlaybackDevice(playbackDevice);
                DiscordAudioStreamBot.getConfig().playbackDevice = playbackDevice;
                saveConfig();
            }
        });
    }

    private void loadConfig() {
        final Config config = DiscordAudioStreamBot.getConfig();

        // general
        botToken.setText(Utils.nullToEmptyString(config.botToken));
        clearLogOnStart.setSelected(config.getClearLogOnStart());

        // voice
        speakEnabled.setSelected(config.getSpeakEnabled());
        updateSpeakEnabled();
        listenEnabled.setSelected(config.getListenEnabled());
        updateListenEnabled();
        {
            DefaultListModel<RecordingDeviceItem> model = new DefaultListModel<>();
            //model.addElement(null);
            BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
            for (int device = 0; Bass.BASS_RecordGetDeviceInfo(device, info); device++) {
                model.addElement(new RecordingDeviceItem(info.getName(), device));
            }
            info.release();
            recordingDevices.setModel(model);
            for (int i = 0; i < model.getSize(); i++) {
                RecordingDeviceItem recordingDevice = model.get(i);
                String recordingDeviceName = recordingDevice != null ? recordingDevice.getName() : null;
                if (Objects.equals(recordingDeviceName, config.recordingDevice)) {
                    recordingDevices.setSelectedIndex(i);
                    break;
                }
            }
        }
        {
            DefaultListModel<PlaybackDeviceItem> model = new DefaultListModel<>();
            //model.addElement(null);
            BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
            for (int device = 0; Bass.BASS_GetDeviceInfo(device, info); device++) {
                model.addElement(new PlaybackDeviceItem(info.getName(), device));
            }
            info.release();
            playbackDevices.setModel(model);
            for (int i = 0; i < model.getSize(); i++) {
                PlaybackDeviceItem playbackDeviceItem = model.get(i);
                String playbackDeviceName = playbackDeviceItem != null ? playbackDeviceItem.getName() : null;
                if (Objects.equals(playbackDeviceName, config.playbackDevice)) {
                    playbackDevices.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveConfig() {
        try {
            DiscordAudioStreamBot.saveConfig();
        } catch (IOException ex) {
            Utils.guiError(this, "Failed to save config", ex);
        }
    }

    private void layoutComponents() {
        int row = 1;
        FormBuilder
                .create()
                .columns(SpecBuilder
                        .create()
                        .add("r:p")
                        .add("f:max(p;100px)")
                        .gap("f:3dlu:g")
                        .add("r:p")
                        .add("f:max(p;100px)")
                        .build()
                )
                .rows(SpecBuilder
                        .create()
                        .add("c:p") // general
                        .add("c:p", 2)
                        .gapUnrelated().add("c:p")
                        .add("c:p")
                        .add("t:p")
                        .build()
                )
                .columnGroups(new int[]{1, 5}, new int[]{2, 6})
                .panel(this)
                .border(BorderFactory.createEmptyBorder(5, 5, 5, 5))
                .addSeparator("General").xyw(1, row, 7)
                .add("Bot token").xy(1, row += 2)
                /**/.add(botToken).xyw(3, row, 5)
                .add("Clear log on startup").xy(1, row += 2)
                /**/.add(clearLogOnStart).xyw(3, row, 5)
                .addSeparator("Audio").xyw(1, row += 2, 7)
                .add("Mute/Unmute").xy(1, row += 2)
                /**/.add(speakEnabled).xy(3, row)
                /**/.add("Deafen/Undeafen").xy(5, row)
                /**/.add(listenEnabled).xy(7, row)
                .add("Input device").xy(1, row += 2)
                /**/.add(recordingDevices).xy(3, row)
                /**/.add("Output device").xy(5, row)
                /**/.add(playbackDevices).xy(7, row)
                .build();
    }

    public void updateLoginStatus(JDA.Status status) {
        switch (status) {
            case SHUTDOWN:
            case FAILED_TO_LOGIN:
                botToken.setEnabled(true);
                break;
            default:
                botToken.setEnabled(false);
                break;
        }
    }

    private void updateSpeakEnabled() {
        boolean enabled = DiscordAudioStreamBot.getConfig().speakEnabled;
        ImageIcon icon = Utils.getIcon("icomoon/32px/031-mic.png", 24, true);
        if(!enabled) {
            icon = new ImageIcon(Utils.overlayImage((BufferedImage) icon.getImage(), Utils.getIcon("runee/32px/strike-through.png", 24, true).getImage()));
        }
        speakEnabled.setIcon(icon);
        recordingDevices.setEnabled(enabled);
    }

    private void updateListenEnabled() {
        boolean enabled = DiscordAudioStreamBot.getConfig().listenEnabled;
        ImageIcon icon = Utils.getIcon("icomoon/32px/017-headphones.png", 24, true);
        if(!enabled) {
            icon = new ImageIcon(Utils.overlayImage((BufferedImage) icon.getImage(), Utils.getIcon("runee/32px/strike-through.png", 24, true).getImage()));
        }
        listenEnabled.setIcon(icon);
        playbackDevices.setEnabled(enabled);
    }
}
