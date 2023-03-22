package net.runee.model;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.HashSet;
import java.util.Set;

public class GuildConfig {
    public String guildId;
    public Set<String> commandChannelIds;
    public String autoJoinVoiceChannelId;
    public String followedUserId;

    public GuildConfig() {

    }

    public GuildConfig(Guild guild) {
        this.guildId = guild.getId();
    }

    public GuildConfig(GuildConfig copy) {
        this.guildId = copy.guildId;
        this.autoJoinVoiceChannelId = copy.autoJoinVoiceChannelId;
        if(copy.commandChannelIds != null) {
            this.commandChannelIds = new HashSet<>(copy.commandChannelIds);
        }
    }

    public void addCommandChannel(MessageChannel channel) {
        if(commandChannelIds == null) {
            commandChannelIds = new HashSet<>();
        }
        commandChannelIds.add(channel.getId());
    }

    public void removeCommandChannel(MessageChannel channel) {
        if(commandChannelIds != null) {
            commandChannelIds.remove(channel.getId());
            if(commandChannelIds.isEmpty()) {
                commandChannelIds = null;
            }
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
