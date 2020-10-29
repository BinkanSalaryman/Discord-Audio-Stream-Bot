package net.runee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.entities.DataMessage;
import net.runee.commands.*;
import net.runee.commands.audio.AutoJoinVoiceCommand;
import net.runee.commands.audio.JoinVoiceCommand;
import net.runee.commands.audio.LeaveVoiceAllCommand;
import net.runee.commands.audio.LeaveVoiceCommand;
import net.runee.errors.BassException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;
import net.runee.misc.logging.Logger;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DiscordAudioStreamBot extends ListenerAdapter {
    private static final Logger logger = new Logger(DiscordAudioStreamBot.class);
    public static final String NAME = "Discord Audio Stream Bot";
    public static final String GITHUB_URL = "https://github.com/BinkanSalaryman/Discord-Audio-Stream-Bot";

    private static DiscordAudioStreamBot instance;
    public static final File configPath = new File("config.json");
    private static Config config;
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create();

    public static DiscordAudioStreamBot getInstance() {
        if (instance == null) {
            instance = new DiscordAudioStreamBot();
        }
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static Config getConfig() {
        if (config == null) {
            // load config
            try {
                if (configPath.exists()) {
                    config = gson.fromJson(Utils.readAllText(configPath), Config.class);
                } else {
                    config = new Config();
                    saveConfig();
                }
            } catch (IOException ex) {
                logger.warn("Failed to load or create new config file", ex);
            }
        }
        return config;
    }

    public static void setConfig(Config config) {
        DiscordAudioStreamBot.config = config;
    }

    public static void saveConfig() throws IOException {
        Utils.writeAllText(configPath, gson.toJson(config));
    }

    // data
    private JDA jda;

    // convenience
    private List<Command> commands;

    private DiscordAudioStreamBot() {

    }

    public void login() throws LoginException {
        logger.info("Logging in...");
        jda = JDABuilder.create(config.botToken,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                //GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES
                //GatewayIntent.DIRECT_MESSAGE_REACTIONS
        )
                .addEventListeners(this)
                .setEnableShutdownHook(false)
                .build();
    }

    public void logoff() {
        logger.info("Logging off...");
        if (jda != null) {
            jda.shutdown();
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public String getInviteUrl() {
        return jda.getInviteUrl(Permission.EMPTY_PERMISSIONS);
    }

    public List<Command> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>(Arrays.asList(
                    // general
                    new AboutCommand(),
                    new BindCommand(),
                    new HelpCommand(),
                    new InviteCommand(),
                    new PrefixCommand(),
                    new ExitCommand(),
                    new StopCommand(),
                    new StatusCommand(),
                    new ActivityCommand(),
                    // audio
                    new JoinVoiceCommand(),
                    new LeaveVoiceCommand(),
                    new LeaveVoiceAllCommand(),
                    new AutoJoinVoiceCommand()
                    // tools
                    //new MessageCommand(),
                    //new TestCommand(),
            ));
        }
        return commands;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent e) {
        for (GuildConfig guildConfig : Utils.nullListToEmpty(getConfig().guildConfigs)) {
            if (guildConfig.autoJoinVoiceChanncelId != null) {
                Guild guild = jda.getGuildById(guildConfig.guildId);
                if (guild == null) {
                    logger.warn("Failed to retrieve guild with id '" + guildConfig.guildId + "' to auto-join voice");
                    continue;
                }
                VoiceChannel channel = guild.getVoiceChannelById(guildConfig.autoJoinVoiceChanncelId);
                if (channel == null) {
                    logger.warn("Voice channel with id '" + guildConfig.autoJoinVoiceChanncelId + "' not found in guild " + guild.getName() + ", removing...");
                    try {
                        saveConfig();
                    } catch (IOException ex) {
                        logger.warn("Failed to save config", ex);
                    }
                    continue;
                }
                joinVoice(channel);
            }
        }
    }

    @Override
    public void onShutdown(@Nonnull ShutdownEvent e) {

    }

    @Override
    public void onStatusChange(@Nonnull StatusChangeEvent e) {
        switch (e.getNewValue()) {
            case CONNECTED:
                logger.info("Logged in");
                break;
            case SHUTDOWN:
                logger.info("Logged off");
                break;
            case FAILED_TO_LOGIN:
                logger.info("Failed to login");
                break;
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent e) {
        String message = e.getMessage().getContentRaw();
        if (e.isFromType(ChannelType.PRIVATE)) {
            onCommandReceived(e, message);
            return;
        }

        final GuildConfig guildConfig = config.getGuildConfig(e.getGuild());

        List<String> prefixes = new ArrayList<>();
        if (guildConfig != null && guildConfig.commandPrefix != null) {
            prefixes.add(guildConfig.commandPrefix);
        }
        prefixes.add("<@" + jda.getSelfUser().getId() + ">");
        prefixes.add("<@!" + jda.getSelfUser().getId() + ">");
        for (String prefix : prefixes) {
            if (message.startsWith(prefix)) {
                onCommandReceived(e, message.substring(prefix.length()).replaceAll("^\\s+", ""));
                return;
            }
        }
    }

    private void onCommandReceived(@Nonnull MessageReceivedEvent e, String cmd) {
        if (e.getAuthor().isBot()) {
            return;
        }

        if (e.getChannel() instanceof GuildChannel) {
            final GuildConfig guildConfig = config.getGuildConfig(e.getGuild());
            if (guildConfig != null && !guildConfig.isCommandChannel(e.getChannel())) {
                if (e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    e.getMessage().delete().queue();
                }
                sendDirect(e.getAuthor(), new EmbedBuilder()
                        .setDescription("Please issue this command in one of the allowed command channels!")
                        .setColor(Utils.colorRed)
                        .build()
                );
                return;
            }
        }

        int nameArgsSeparatorIdx = cmd.indexOf(" ");
        final String name;
        final String[] args;
        if (nameArgsSeparatorIdx >= 0) {
            name = cmd.substring(0, nameArgsSeparatorIdx);
            args = Utils.parseCommandArgs(cmd.substring(nameArgsSeparatorIdx + 1));
        } else {
            name = cmd;
            args = new String[0];
        }

        for (Command command : getCommands()) {
            if (command.getName().equals(name)) {
                CommandContext ctx = new CommandContext(e);
                ctx.run(command, args);
                return;
            }
        }
        e.getChannel().sendMessage(new EmbedBuilder()
                .setDescription("Unrecognized command: `" + name + "`!")
                .setColor(Utils.colorRed)
                .build()).queue();
    }

    public void sendDirect(User user, Message message) {
        user.openPrivateChannel().queue(chan -> {
            chan.sendMessage(message).queue();
        });
    }

    public void sendDirect(User user, String message) {
        sendDirect(user, new DataMessage(false, message, null, null));
    }

    public void sendDirect(User user, MessageEmbed embed) {
        sendDirect(user, new DataMessage(false, "", null, embed));
    }

    public void joinVoice(VoiceChannel channel) {
        AudioManager audioManager = channel.getGuild().getAudioManager();
        updateSpeakState(audioManager, null, null);
        updateListenState(audioManager, null, null);
        audioManager.setConnectionListener(new ConnectionListener() {
            @Override
            public void onPing(long ping) {

            }

            @Override
            public void onStatusChange(@Nonnull ConnectionStatus status) {
                try {
                    switch (status) {
                        case CONNECTED: {
                            AudioSendHandler sendingHandler = audioManager.getSendingHandler();
                            if (sendingHandler instanceof SpeakHandler) {
                                ((SpeakHandler) sendingHandler).setPlaying(true);
                            }
                            break;
                        }
                        default: {
                            AudioSendHandler sendingHandler = audioManager.getSendingHandler();
                            if (sendingHandler instanceof SpeakHandler) {
                                ((SpeakHandler) sendingHandler).setPlaying(false);
                            }
                            break;
                        }
                    }
                } catch (BassException ex) {
                    logger.error("Failed to pause/unpause speak handler for guild " + audioManager.getGuild().getName(), ex);
                }
            }

            @Override
            public void onUserSpeaking(@Nonnull User user, boolean speaking) {

            }
        });
        audioManager.openAudioConnection(channel);
    }

    public void leaveVoice(Guild guild) {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            updateSpeakState(audioManager, false, null);
            updateListenState(audioManager, false, null);
            audioManager.closeAudioConnection();
        }
    }

    public void leaveVoiceAll() {
        for (AudioManager audioManager : jda.getAudioManagers()) {
            leaveVoice(audioManager.getGuild());
        }
    }

    public void updateSpeakState(AudioManager audioManager, Boolean speakEnabled, String recordingDevice) {
        speakEnabled = speakEnabled != null ? speakEnabled : config.getSpeakEnabled();
        recordingDevice = recordingDevice != null ? recordingDevice : config.recordingDevice;

        // audio send handler
        AudioSendHandler sendingHandler = audioManager.getSendingHandler();
        if (speakEnabled) {
            if (sendingHandler == null) {
                sendingHandler = new SpeakHandler();
            }
            if (sendingHandler instanceof SpeakHandler) {
                try {
                    ((SpeakHandler) sendingHandler).openRecordingDevice(Utils.getRecordingDeviceHandle(recordingDevice));
                } catch (BassException ex) {
                    logger.error("Failed to open recording device '" + recordingDevice + "'", ex);
                    sendingHandler = null;
                    speakEnabled = false;
                }
            }
        } else {
            if (sendingHandler != null) {
                if (sendingHandler instanceof Closeable) {
                    Utils.closeQuiet((Closeable) sendingHandler);
                }
                sendingHandler = null;
            }
        }
        audioManager.setSendingHandler(sendingHandler);
        audioManager.setSelfMuted(!speakEnabled);
    }

    public void updateListenState(AudioManager audioManager, Boolean listenEnabled, String playbackDevice) {
        listenEnabled = listenEnabled != null ? listenEnabled : config.getListenEnabled();
        playbackDevice = playbackDevice != null ? playbackDevice : config.playbackDevice;

        // audio receive handler
        AudioReceiveHandler receivingHandler = audioManager.getReceivingHandler();
        if (listenEnabled) {
            if (receivingHandler == null) {
                receivingHandler = new ListenHandler();
            }
            if (receivingHandler instanceof ListenHandler) {
                try {
                    ((ListenHandler) receivingHandler).openPlaybackDevice(Utils.getPlaybackDeviceHandle(playbackDevice));
                } catch (BassException ex) {
                    logger.error("Failed to open playback device '" + playbackDevice + "'", ex);
                    receivingHandler = null;
                    listenEnabled = false;
                }
            }
        } else {
            if (receivingHandler != null) {
                if (receivingHandler instanceof Closeable) {
                    Utils.closeQuiet((Closeable) receivingHandler);
                }
                receivingHandler = null;
            }
        }
        audioManager.setReceivingHandler(receivingHandler);
        audioManager.setSelfDeafened(!listenEnabled);
    }

    public void setSpeakEnabled(boolean speakEnabled) {
        for (AudioManager audioManager : getConnectedAudioManagers()) {
            updateSpeakState(audioManager, speakEnabled, null);
        }
    }

    public void setListenEnabled(boolean listenEnabled) {
        for (AudioManager audioManager : getConnectedAudioManagers()) {
            updateListenState(audioManager, listenEnabled, null);
        }
    }

    public void setRecordingDevice(String recordingDevice) {
        for (AudioManager audioManager : getConnectedAudioManagers()) {
            updateSpeakState(audioManager, null, recordingDevice);
        }
    }

    public void setPlaybackDevice(String playbackDevice) {
        for (AudioManager audioManager : getConnectedAudioManagers()) {
            updateListenState(audioManager, null, playbackDevice);
        }
    }

    private List<AudioManager> getConnectedAudioManagers() {
        if (jda != null) {
            List<AudioManager> result = new ArrayList<>();
            for (AudioManager audioManager : jda.getAudioManagers()) {
                if (audioManager.isConnected()) {
                    result.add(audioManager);
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }
}
