package net.runee.model;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;

public class GuildConfig {
    public String guildId;
    public String commandPrefix;
    public List<String> commandChannelIds;

    public GuildConfig() {

    }

    public GuildConfig(Guild guild) {
        this.guildId = guild.getId();
    }

    public GuildConfig(GuildConfig copy) {
        this.guildId = copy.guildId;
        this.commandPrefix = copy.commandPrefix;
        if(copy.commandChannelIds != null) {
            this.commandChannelIds = new ArrayList<>(copy.commandChannelIds);
        }
    }

    public void addCommandChannel(MessageChannel channel) {
        if(commandChannelIds == null) {
            commandChannelIds = new ArrayList<>();
        }
        commandChannelIds.add(channel.getId());
    }

    public void removeCommandChannel(MessageChannel channel) {
        if(commandChannelIds != null) {
            commandChannelIds.remove(channel.getId());
        }
    }

    public boolean isCommandChannel(MessageChannel channel) {
        if(commandChannelIds != null) {
            return commandChannelIds.contains(channel.getId());
        } else {
            return true; // default is that every channel is a command channel!
        }
    }
}
