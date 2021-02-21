package net.runee.model;

import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Config {
    public String botToken;
    public Boolean speakEnabled;
    public String recordingDevice;
    public Boolean listenEnabled;
    public String playbackDevice;
    public Boolean clearLogOnStart;
    public List<GuildConfig> guildConfigs;

    public Config() {

    }

    public Config(Config copy, boolean copyGuildConfigs) {
        this.botToken = copy.botToken;
        this.speakEnabled = copy.speakEnabled;
        this.recordingDevice = copy.recordingDevice;
        this.listenEnabled = copy.listenEnabled;
        this.playbackDevice = copy.playbackDevice;
        this.clearLogOnStart = copy.clearLogOnStart;
        if(copyGuildConfigs && copy.guildConfigs != null) {
            this.guildConfigs = new ArrayList<>();
            for (GuildConfig guildConfig : copy.guildConfigs) {
                this.guildConfigs.add(new GuildConfig(guildConfig));
            }
        }
    }

    public boolean getSpeakEnabled() {
        return speakEnabled != null ? speakEnabled : false;
    }

    public boolean getListenEnabled() {
        return listenEnabled != null ? listenEnabled : false;
    }

    public GuildConfig getGuildConfig(Guild guild) {
        if(guild != null && guildConfigs != null) {
            for (GuildConfig guildConfig : guildConfigs) {
                if (guildConfig == null) {
                    continue; // what?
                }
                if (Objects.equals(guildConfig.guildId, guild.getId())) {
                    return guildConfig;
                }
            }
        }
        GuildConfig result = new GuildConfig(guild);
        if(guildConfigs == null) {
            guildConfigs = new ArrayList<>();
        }
        guildConfigs.add(result);
        return result;
    }

    public boolean getClearLogOnStart() {
        return clearLogOnStart != null ? clearLogOnStart : true;
    }
}
