package net.runee.misc;

import com.jgoodies.forms.builder.FormBuilder;
import jouvieje.bass.Bass;
import jouvieje.bass.defines.BASS_ERROR;
import jouvieje.bass.structures.BASS_DEVICEINFO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.runee.errors.BassException;
import net.runee.misc.gui.SpecBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public final class Utils {
    private Utils() {
    }

    public static final Color colorYellow = Color.decode("#f0ff40");
    public static final Color colorGreen = Color.decode("#80f040");
    public static final Color colorRed = Color.decode("#f04040");
    public static final Color colorBlack = Color.decode("#000014");

    public static final String ucListItem = "・";

    public static final int numMaxEmbedFields = 25;

    // private
    private static Map<Integer, BufferedImage> missingIconCache = new HashMap<>();

    public static String readAllText(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    public static void writeAllText(File file, String text) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.write(text);
        }
    }

    public static BufferedImage missingIcon(int size) {
        BufferedImage result = missingIconCache.get(size);
        if (result == null) {
            result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = result.createGraphics();
            for (int i = 0; i < size; i++) {
                g.setColor(i % 2 == 0 ? Color.magenta : Color.black);
                g.drawLine(i, 0, 0, i);
                g.setColor(i % 2 != (size % 2) ? Color.magenta : Color.black);
                g.drawLine(size - 1, i, i, size - 1);
            }
            g.dispose();
            missingIconCache.put(size, result);
        }
        return result;
    }

    public static BufferedImage downloadImage(String urlStr) {
        try {
            final URL url = new URL(urlStr);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
            return ImageIO.read(conn.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static BufferedImage aspectFit(BufferedImage image, int width, int height) {
        double scalex = (double) width / image.getWidth();
        double scaley = (double) height / image.getHeight();
        double scale = Math.min(scalex, scaley);

        int w = (int) (image.getWidth(null) * scale);
        int h = (int) (image.getHeight(null) * scale);

        BufferedImage resized = new BufferedImage(w, h, image.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(image.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public static JPanel buildFlowPanel(JComponent... components) {
        FormBuilder form = FormBuilder
                .create()
                .columns(SpecBuilder
                        .create()
                        .add("f:p", components.length)
                        .build()
                )
                .rows("f:p:g");
        if (components.length > 1) {
            int[] columnGroup = new int[components.length];
            for (int i = 0; i < components.length; i++) {
                columnGroup[i] = 1 + i * 2;
            }
            form.columnGroup(columnGroup);
        }
        for (int i = 0; i < components.length; i++) {
            form.add(components[i]).xy(1 + i * 2, 1);
        }
        return form.build();
    }

    public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0, lastNotifiedChange = 0;

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document) e.getOldValue();
            Document d2 = (Document) e.getNewValue();
            if (d1 != null) d1.removeDocumentListener(dl);
            if (d2 != null) d2.addDocumentListener(dl);
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) d.addDocumentListener(dl);
    }

    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Long tryParseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Double tryParseDouble(String text) {
        Double val;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String emptyStringToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    public static String nullToEmptyString(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    public static <T> List<T> nullListToEmpty(List<T> list) {
        if (list != null) {
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public static ImageIcon getIcon(String file, int size, boolean useMissingIcon) {
        file = "/net/runee/resources/icons/" + file;
        InputStream stream = Utils.class.getResourceAsStream(file);
        if(stream != null) {
            try {
                return new ImageIcon(aspectFit(ImageIO.read(stream), size, size));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return useMissingIcon ? new ImageIcon(missingIcon(size)) : null;
    }

    public static void guiError(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void guiError(Component parentComponent, String message, Throwable reason) {
        guiError(parentComponent, message + "\nReason: " + reason.getMessage());
    }

    public static void closeQuiet(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void checkBassError() throws BassException {
        int error = Bass.BASS_ErrorGetCode();
        if (error != BASS_ERROR.BASS_OK) {
            throw new BassException(error);
        }
    }

    public static List<VoiceChannel> findSuitableVoiceChannel(Guild guild, Member author) {
        for (int step = 0; true; step++) {
            switch (step) {
                case 0: {
                    // try to connect to author's channel
                    VoiceChannel channel = author.getVoiceState().getChannel();
                    if (channel == null) {
                        continue;
                    }
                    return Collections.singletonList(channel);
                }
                case 1: {
                    // try to connect to AFK channel
                    VoiceChannel channel = guild.getAfkChannel();
                    if (channel == null) {
                        continue;
                    }
                    return Collections.singletonList(channel);
                }
                case 2:
                    // try to connect to any channel
                    return guild.getVoiceChannels();
                default:
                    return Collections.emptyList();
            }
        }
    }

    public static List<VoiceChannel> findVoiceChannel(Guild guild, String search) {
        for (int step = 0; true; step++) {
            switch (step) {
                case 0: {
                    Long channelId = Utils.tryParseLong(search);
                    if (channelId == null) {
                        continue;
                    }
                    VoiceChannel channel = guild.getVoiceChannelById(channelId);
                    if (channel == null) {
                        continue;
                    }
                    return Collections.singletonList(channel);
                }
                case 1: {
                    List<VoiceChannel> channels = guild.getVoiceChannelsByName(search, true);
                    if (channels.size() != 1) {
                        continue;
                    }
                    return Collections.singletonList(channels.get(0));
                }
                case 2:
                    return guild.getVoiceChannelsByName(search, false);
                default:
                    return Collections.emptyList();
            }
        }
    }

    public static List<TextChannel> findTextChannel(Guild guild, String search) {
        for (int step = 0; true; step++) {
            switch (step) {
                case 0: {
                    Long channelId = Utils.tryParseLong(search);
                    if (channelId == null) {
                        continue;
                    }
                    TextChannel channel = guild.getTextChannelById(channelId);
                    if (channel != null) {
                        return Collections.singletonList(channel);
                    }
                    continue;
                }
                case 1: {
                    List<TextChannel> channels = guild.getTextChannelsByName(search, true);
                    if (channels.size() != 1) {
                        continue;
                    }
                    return Collections.singletonList(channels.get(0));
                }
                case 2:
                    return guild.getTextChannelsByName(search, true);
                default:
                    return Collections.emptyList();
            }
        }
    }

    public static List<Guild> findGuild(JDA jda, String search) {
        for (int step = 0; true; step++) {
            switch (step) {
                case 0: {
                    Long guildId = Utils.tryParseLong(search);
                    if (guildId == null) {
                        continue;
                    }
                    Guild guild = jda.getGuildById(guildId);
                    if (guild == null) {
                        continue;
                    }
                    return Collections.singletonList(guild);
                }
                case 1: {
                    List<Guild> guilds = jda.getGuildsByName(search, true);
                    if (guilds.size() != 1) {
                        continue;
                    }
                    return Collections.singletonList(guilds.get(0));
                }
                case 2:
                    return jda.getGuildsByName(search, false);
                default:
                    return Collections.emptyList();
            }
        }
    }

    public static String[] parseCommandArgs(String args) {
        final char ESCAPE_CHAR = '\\';
        final char STRING_CHAR = '"';
        final char ARGSEP_CHAR = ' ';

        List<String> result = new ArrayList<>();
        StringBuilder arg = new StringBuilder();
        boolean isString = false;
        boolean isEscaped = false;
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (isEscaped) {
                arg.append(c);
                isEscaped = false;
                continue;
            }
            switch (c) {
                case STRING_CHAR:
                    isString = !isString;
                    break;
                case ARGSEP_CHAR:
                    if (isString) {
                        arg.append(c);
                    } else {
                        if(i > 0 &&  args.charAt(i-1) != ARGSEP_CHAR) {
                            result.add(arg.toString());
                            arg.delete(0, arg.length());
                        }
                    }
                    break;
                case ESCAPE_CHAR:
                    isEscaped = true;
                    break;
                default:
                    arg.append(c);
                    break;
            }
        }
        result.add(arg.toString());
        arg.delete(0, arg.length());
        return result.toArray(new String[0]);
    }

    public static int getRecordingDeviceHandle(String deviceName) {
        if (deviceName != null) {
            BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
            for (int device = 0; Bass.BASS_RecordGetDeviceInfo(device, info); device++) {
                if (deviceName.equals(info.getName())) {
                    info.release();
                    return device;
                }
            }
            info.release();
            throw new RuntimeException("Recording device '" + deviceName + "' not found!");
        } else {
            return 0;
        }
    }

    public static int getPlaybackDeviceHandle(String deviceName) {
        if (deviceName != null) {
            BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
            for (int device = 0; Bass.BASS_GetDeviceInfo(device, info); device++) {
                if (deviceName.equals(info.getName())) {
                    info.release();
                    return device;
                }
            }
            info.release();
            throw new RuntimeException("Playback device '" + deviceName + "' not found!");
        } else {
            return 0;
        }
    }
}
