package net.runee.gui;

import net.dv8tion.jda.api.JDA;
import net.runee.DiscordAudioStreamBot;
import net.runee.gui.components.MaintenancePanel;
import net.runee.gui.components.HomePanel;
import net.runee.gui.components.SettingsPanel;
import net.runee.misc.Utils;
import net.runee.misc.gui.BorderPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements Runnable {
    static {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new MainFrame());
    }

    private JTabbedPane tabs;
    private int idxHome;
    private HomePanel tabHome;
    private int idxMaintain;
    private MaintenancePanel tabMaintain;
    private int idxSettings;
    private SettingsPanel tabSettings;

    public MainFrame() {
        updateTitle();
        setIconImage(Utils.getIcon("icomoon/32px/017-headphones.png", 32, true).getImage());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        initComponents();
        layoutComponents();

        setMinimumSize(new Dimension(800, 600));
        pack();
    }

    private void initComponents() {
        tabs = new JTabbedPane();
        tabHome = new HomePanel(this);
        tabMaintain = new MaintenancePanel();
        tabSettings = new SettingsPanel();

        // home
        idxHome = tabs.getTabCount();
        tabs.addTab("Home", getTabIcon("001-home"), new BorderPanel(tabHome));

        // maintenance
        idxMaintain = tabs.getTabCount();
        tabs.addTab("Maintenance", getTabIcon("146-wrench"), new BorderPanel(tabMaintain));
        tabs.setEnabledAt(idxMaintain, false);

        // settings
        idxSettings = tabs.getTabCount();
        tabs.addTab("Settings", getTabIcon("190-menu"), new BorderPanel(tabSettings));
    }

    private Icon getTabIcon(String file) {
        return Utils.getIcon("icomoon/32px/" + file + ".png", 24, true);
    }

    private void layoutComponents() {
        setContentPane(tabs);
    }

    public void updateLoginStatus(JDA.Status status) {
        updateTitle();
        tabs.setEnabledAt(idxMaintain, status == JDA.Status.CONNECTED);
        tabMaintain.updateLoginStatus(status);
        tabSettings.updateLoginStatus(status);
    }

    @Override
    public void run() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateTitle() {
        final JDA jda = DiscordAudioStreamBot.getInstance().getJDA();
        JDA.Status status = jda != null ? jda.getStatus() : JDA.Status.SHUTDOWN;
        String title = "Discord Audio Stream Bot - " + format(status);
        if (status == JDA.Status.CONNECTED) {
            title += " [" + jda.getSelfUser().getName() + "]";
        }
        setTitle(title);
    }

    private String format(JDA.Status status) {
        String[] words = status.name().replace("_", " ").split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            words[i] = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }
        return String.join(" ", words);
    }
}
