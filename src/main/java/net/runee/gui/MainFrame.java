package net.runee.gui;

import com.jgoodies.common.base.SystemUtils;
import jouvieje.bass.BassInit;
import net.dv8tion.jda.api.JDA;
import net.runee.DiscordAudioStreamBot;
import net.runee.gui.components.MaintenancePanel;
import net.runee.gui.components.HomePanel;
import net.runee.gui.components.SettingsPanel;
import net.runee.misc.Utils;
import net.runee.misc.gui.BorderPanel;
import net.runee.misc.logging.Logger;
import net.runee.misc.logging.appender.Appender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame implements Runnable {
    private static final Logger logger = new Logger(MainFrame.class);
    private static MainFrame instance;

    public static void main(String[] args) {
        // add shutdown hook
        Thread shutdownThread = new Thread(MainFrame::onRuntimeShutdown);
        shutdownThread.setName("DASB Shutdown Hook");
        shutdownThread.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        Thread.setDefaultUncaughtExceptionHandler(MainFrame::uncaughtException);

        if (DiscordAudioStreamBot.getConfig().getClearLogOnStart()) {
            Logger.logPath.delete();
        }

        logger.info("Hello World!");

        // set L&F
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                System.setProperty("swing.systemlaf", "com.jgoodies.looks.windows.WindowsLookAndFeel");
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.warn("Failed to set L&F", ex);
        }

        // load libraries
        BassInit.loadLibraries();

        // run app
        EventQueue.invokeLater(getInstance());
    }

    public static MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    private static void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread " + t.getName(), e);
        JOptionPane.showMessageDialog(instance, "A fatal error occurred and the application will be closed.\nThe logs can be found at " + Logger.logPath.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }

    private static void onRuntimeShutdown() {
        logger.info("Goodbye!");
    }

    private JTabbedPane tabs;
    private int idxHome;
    private HomePanel tabHome;
    private int idxMaintain;
    private MaintenancePanel tabMaintain;
    private int idxSettings;
    private SettingsPanel tabSettings;

    private MainFrame() {
        updateTitle();
        setIconImage(Utils.getIcon("icomoon/32px/017-headphones.png", 32, true).getImage());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JDA jda = DiscordAudioStreamBot.getInstance().getJDA();
                if (jda != null) {
                    jda.shutdownNow();
                }
                MainFrame.this.dispose();
                System.exit(0);
            }
        });

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
        String title = DiscordAudioStreamBot.NAME + " - " + format(status);
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
