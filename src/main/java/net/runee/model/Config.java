package net.runee.model;

import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Config {
    public String botToken;
    public Boolean autoLogin;
    public Boolean speakEnabled;
    public String recordingDevice;
    public Boolean listenEnabled;
    public String playbackDevice;
    public List<GuildConfig> guildConfigs;
    public Boolean speakThresholdEnabled;

    public Double speakThreshold;

    public Config() {

    }

    public Config(Config copy, boolean copyGuildConfigs) {
        this.botToken = copy.botToken;
        this.autoLogin = copy.autoLogin;
        this.speakEnabled = copy.speakEnabled;
        this.recordingDevice = copy.recordingDevice;
        this.listenEnabled = copy.listenEnabled;
        this.playbackDevice = copy.playbackDevice;
        if(copyGuildConfigs && copy.guildConfigs != null) {
            this.guildConfigs = new ArrayList<>();
            for (GuildConfig guildConfig : copy.guildConfigs) {
                this.guildConfigs.add(new GuildConfig(guildConfig));
            }
        }
    }

    public boolean isAutoLogin() {
        return autoLogin != null ? autoLogin : false;
    }

    public boolean getSpeakEnabled() {
        return speakEnabled != null ? speakEnabled : false;
    }

    public boolean getListenEnabled() {
        return listenEnabled != null ? listenEnabled : false;
    }

    public boolean getSpeakThresholdEnabled() {
        return speakThresholdEnabled != null ? speakThresholdEnabled : false;
    }

    public double getSpeakThreshold() {
        return speakThreshold != null ? speakThreshold : 0.5d;
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
}
