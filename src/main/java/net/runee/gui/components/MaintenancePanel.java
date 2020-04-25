package net.runee.gui.components;

import com.jgoodies.forms.builder.FormBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.hooks.EventListener;
import net.runee.DiscordAudioStreamBot;
import net.runee.gui.renderer.GuildListCellRenderer;
import net.runee.misc.Utils;
import net.runee.misc.gui.SpecBuilder;

import javax.annotation.Nonnull;
import javax.swing.*;

public class MaintenancePanel extends JPanel implements EventListener {
    // components
    private JList<Guild> guilds;
    private JButton addGuild;
    private JButton removeGuild;
    // convenience
    private DefaultListModel<Guild> guildsModel;

    public MaintenancePanel() {
        initModels();
        initComponents();
        layoutComponents();
    }

    private void initModels() {
        guildsModel = new DefaultListModel<>();
    }

    private void initComponents() {
        guilds = new JList<>();
        guilds.setModel(guildsModel);
        guilds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        guilds.setCellRenderer(GuildListCellRenderer.getInstance());
        guilds.addListSelectionListener(e -> updateGuildControls());
        addGuild = new JButton("Invite bot...");
        addGuild.addActionListener(e -> {
            if(!Utils.browseUrl(DiscordAudioStreamBot.getInstance().getInviteUrl())) {
                JOptionPane.showMessageDialog(this, "Unable to open invite url in browser.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        removeGuild = new JButton("Leave guild");
        removeGuild.addActionListener(e -> {
            removeGuild.setEnabled(false);
            Guild guild = guilds.getSelectedValue();
            guild.leave().queue(e2 -> {
                int index = guilds.getSelectedIndex();
                guildsModel.removeElementAt(index);
                if (index == guildsModel.size()) {
                    index--;
                }
                if (index >= 0) {
                    guilds.setSelectedIndex(index);
                }

                removeGuild.setEnabled(true);
                updateGuildControls();
            });
        });
    }

    private void layoutComponents() {
        int row;
        FormBuilder
                .create()
                .columns("f:p:g")
                .rows(SpecBuilder
                        .create()
                        .add("c:p") // guilds
                        .add("f:p:g")
                        .add("c:p")
                        .build()
                )
                .panel(this)
                .border(BorderFactory.createEmptyBorder(5, 5, 5, 5))
                .addSeparator("Guilds").xyw(1, row = 1, 1)
                .add(guilds).xy(1, row += 2)
                .add(Utils.buildFlowPanel(addGuild, removeGuild)).xy(1, row += 2)
                .build();
    }

    private void updateGuilds() {
        final JDA jda = DiscordAudioStreamBot.getInstance().getJDA();
        guildsModel.clear();
        for (Guild guild : jda.getGuilds()) {
            guildsModel.addElement(guild);
        }
        updateGuildControls();
    }

    public void updateLoginStatus(JDA.Status status) {
        switch (status) {
            case CONNECTED:
                final JDA jda = DiscordAudioStreamBot.getInstance().getJDA();
                updateGuilds();
                jda.addEventListener(this);
                break;
        }
    }

    private void updateGuildControls() {
        removeGuild.setEnabled(guilds.getSelectedValue() != null);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent e) {
        if(e instanceof GenericGuildEvent) {
            final Guild guild = ((GenericGuildEvent) e).getGuild();
            if (e instanceof GuildJoinEvent) {
                guildsModel.addElement(guild);
                updateGuildControls();
            }
            if(e instanceof GuildLeaveEvent) {
                GuildListCellRenderer.getInstance().clearIconCache(guild);
            }
        }
    }
}
