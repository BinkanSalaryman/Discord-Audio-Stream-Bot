package net.runee.gui.components;

import com.jgoodies.forms.builder.FormBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.runee.DiscordAudioStreamBot;
import net.runee.gui.MainFrame;
import net.runee.misc.Utils;
import net.runee.misc.gui.SpecBuilder;
import net.runee.misc.logging.Logger;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import javax.swing.*;

public class HomePanel extends JPanel implements EventListener {
    private static final Logger logger = new Logger(HomePanel.class);
    private JLabel loginLabel;
    private JButton loginButton;
    private MainFrame mainFrame;

    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        layoutComponents();
        updateLoginStatus(JDA.Status.SHUTDOWN, true);
    }

    private void initComponents() {
        loginLabel = new JLabel();
        loginLabel.setHorizontalAlignment(SwingConstants.CENTER);

        loginButton = new JButton("", Utils.getIcon("icomoon/32px/183-switch.png", 32, true));
        loginButton.addActionListener(e -> {
            loginButton.setEnabled(false);
            final DiscordAudioStreamBot bot = DiscordAudioStreamBot.getInstance();
            switch (bot.getJDA() != null ? bot.getJDA().getStatus() : JDA.Status.SHUTDOWN) {
                case CONNECTED:
                    bot.logoff();
                    break;
                case SHUTDOWN:
                case FAILED_TO_LOGIN:
                    if (DiscordAudioStreamBot.getConfig().botToken == null) {
                        JOptionPane.showMessageDialog(HomePanel.this, "A bot token must be set.", "Error", JOptionPane.ERROR_MESSAGE);
                        loginButton.setEnabled(true);
                        return;
                    }
                    try {
                        bot.login();
                        bot.getJDA().addEventListener(this);
                    } catch (LoginException ex) {
                        logger.error("Failed to log in", ex);
                        JOptionPane.showMessageDialog(HomePanel.this, "Failed to log in:\n\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        loginButton.setEnabled(true);
                    }
                    break;
            }
        });
    }

    private void layoutComponents() {
        FormBuilder
                .create()

                .columns("f:p:g")
                .rows(SpecBuilder
                        .create()
                        .add("f:p:g")
                        .build()
                )
                .panel(this)
                .add(buildCenterPanel()).xy(1, 1, "c,c")
                .build();
    }

    private JPanel buildCenterPanel() {
        JPanel result = new JPanel();
        result.setLayout(null);
        result.setBackground(Utils.colorBlack);
        result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Utils.colorYellow, 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        result.setSize(192, 192);
        result.setMinimumSize(result.getSize());
        result.setPreferredSize(result.getSize());
        result.setMaximumSize(result.getSize());

        loginButton.setSize(96, 96);
        loginButton.setLocation(result.getWidth() / 2 - loginButton.getWidth() / 2, result.getHeight() / 2 - loginButton.getHeight() / 2);
        result.add(loginButton);

        loginLabel.setSize(result.getWidth(), loginButton.getY());
        result.add(loginLabel);

        return result;
    }

    @Override
    public void onEvent(@Nonnull GenericEvent e) {
        if (e instanceof StatusChangeEvent) {
            updateLoginStatus(((StatusChangeEvent) e).getNewValue(), false);
        }
    }

    public void updateLoginStatus(JDA.Status status, boolean initial) {
        loginLabel.setText("<html><center>Status:<br>"+format(status)+"</center></html>");
        switch (status) {
            case CONNECTED:
                loginLabel.setForeground(Utils.colorGreen);
                break;
            case SHUTDOWN:
            case FAILED_TO_LOGIN:
                loginLabel.setForeground(Utils.colorRed);
                break;
            default:
                loginLabel.setForeground(Utils.colorYellow);
                break;
        }
        switch (status) {
            case CONNECTED:
            case SHUTDOWN:
            case FAILED_TO_LOGIN:
                loginButton.setEnabled(true);
                break;
            default:
                loginButton.setEnabled(false);
                break;
        }
        if (!initial) {
            mainFrame.updateLoginStatus(status);
        }
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
