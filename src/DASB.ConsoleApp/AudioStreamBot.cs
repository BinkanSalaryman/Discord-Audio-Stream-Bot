using Discord;
using Discord.Audio;
using Discord.Commands;
using Discord.WebSocket;
using Microsoft.Extensions.DependencyInjection;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using Un4seen.Bass;

namespace DASB {
    public class AudioStreamBot : IDisposable {
        public class CommandContext : ICommandContext {
            public AudioStreamBot Bot { get; private set; }
            public SocketUserMessage Message { get; private set; }
            public SocketGuild Guild { get; private set; }
            public DiscordSocketClient Discord => Bot.discord;
            public Config Config => Bot.config;
            public IBotAgent Agent => Bot.agent;

            IDiscordClient ICommandContext.Client => Bot.discord;
            IGuild ICommandContext.Guild => Guild;
            IMessageChannel ICommandContext.Channel => Message.Channel;
            IUser ICommandContext.User => Message.Author;
            IUserMessage ICommandContext.Message => Message;

            public CommandFeedback Feedback = CommandFeedback.Default;

            public CommandContext(AudioStreamBot bot, SocketUserMessage message, SocketGuild guild) {
                this.Bot = bot;
                this.Message = message;
                this.Guild = guild;
            }

            public void SaveConfig() {
                File.WriteAllText(configPath, Config.ToString());
            }
        }

        private const int frequency = 48000;
        private const int channels = 2;
        private static string configPath => Path.Combine(Directory.GetParent(Assembly.GetExecutingAssembly().Location).FullName, "config.json");
        public static IServiceProvider Services { get; private set; }
        private static IServiceCollection services = new ServiceCollection();

        public static void AddServices(Action<IServiceCollection> modify) {
            modify(services);
            Services = services.BuildServiceProvider();
        }

        static AudioStreamBot() {
            AddServices(x =>
                x.AddScoped(typeof(IBotAgent), typeof(BotAgentTomoko))
            );
        }

        // general
        private Config config;
        private IBotAgent agent;
        private DiscordSocketClient discord;
        private CommandService commands;
        private WinApi.HandlerRoutine consoleCtrlHandler;

        // speak
        private ConcurrentDictionary<ulong, VoiceSet> voiceSets = new ConcurrentDictionary<ulong, VoiceSet>();
        private int recordDevice;
        private int recordChannel;
        private RECORDPROC recordProc;

        // listen
        private ConcurrentDictionary<ulong, AudioInStream> listenStreams = new ConcurrentDictionary<ulong, AudioInStream>();
        private readonly object playbackStream_writeLock = new object();
        private int playbackDevice;
        private int playbackChannel;
        private STREAMPROC playProc;
        private bool firstPlayProcCall;
        private MemoryStream playbackBuffer;

        private static void Main() {
            Console.Title = Assembly.GetExecutingAssembly().GetName().Name;
            CultureInfo.DefaultThreadCurrentCulture = CultureInfo.InvariantCulture;
            CultureInfo.DefaultThreadCurrentUICulture = CultureInfo.InvariantCulture;
            BassNet.Registration("poo@poo.com", "2X25242411252422");

            var cfg = Config.Init(configPath);
            if (cfg != null) {
                MainAsync(cfg).GetAwaiter().GetResult();
            } else {
                Console.ReadKey();
            }
        }

        private static async Task MainAsync(Config cfg) {
            using (var bot = new AudioStreamBot(cfg)) {
                if (await bot.Init()) {
                    await bot.Start();
                }
                await Task.Delay(-1);
            }
        }

        public AudioStreamBot(Config config) {
            this.config = config;
        }

        private async Task<bool> Init() {
            // capture console exit
            consoleCtrlHandler = new WinApi.HandlerRoutine(consoleCtrl);
            WinApi.SetConsoleCtrlHandler(consoleCtrlHandler, true);

            // create discord client
            discord = new DiscordSocketClient();
            discord.MessageReceived += Discord_MessageReceived;
            discord.Log += Discord_Log;
            discord.Ready += Discord_Ready;

            // install commands
            commands = new CommandService();
            AddServices(x =>
                x.AddSingleton(discord)
                .AddSingleton(commands)
                .BuildServiceProvider()
            );

            var agents = Services.GetServices<IBotAgent>();
            agent = agents.SingleOrDefault(a => a.Id == config.commandsBotAgent);
            if (agent == null) {
                Log(LogSeverity.Error, "Bot agent \"" + config.commandsBotAgent + "\" not found, available agents:\n" + string.Join("\n * ", agents.Select(a => a.Id)));
                return false;
            }
            await commands.AddModuleAsync(typeof(Commands));
            return true;
        }

        private async Task Discord_MessageReceived(SocketMessage messageParam) {
            if (!config.commandsEnabled) {
                return;
            }

            var message = messageParam as SocketUserMessage;
            if (message == null || message.Author.Id == discord.CurrentUser.Id) {
                return;
            }

            int argPos = 0;
            bool isCommand = message.HasMentionPrefix(discord.CurrentUser, ref argPos) || message.Channel is IDMChannel;
            if (!isCommand) {
                return;
            }

            Task.Run(() => runCommand(message, argPos));
        }

        private async Task runCommand(SocketUserMessage message, int argPos) {
            Log(LogSeverity.Verbose, message.Author.Username + "#" + message.Author.Discriminator + ": " + message.Content.Substring(argPos));

            // run command
            var context = new CommandContext(this, message, (message.Channel as SocketGuildChannel)?.Guild);
            var result = await commands.ExecuteAsync(context, argPos, Services);

            if (result.IsSuccess) {
                switch (context.Feedback) {
                    case CommandFeedback.Default:
                        await context.Message.AddReactionAsync(Commands.emoji_success);
                        break;
                    case CommandFeedback.NoEntry:
                        await message.AddReactionAsync(Commands.emoji_forbidden);
                        break;
                    case CommandFeedback.Warning:
                        await message.AddReactionAsync(Commands.emoji_warning);
                        break;
                    case CommandFeedback.GuildContextRequired:
                        await message.AddReactionAsync(Commands.emoji_warning);
                        await message.Channel.SendMessageAsync("Please try this in a guild text channel");
                        break;
                }
            } else {
                switch (result.Error) {
                    case CommandError.Exception:
                        Log(LogSeverity.Error, "Command failed: " + result.ErrorReason);
                        await message.Channel.SendMessageAsync(agent.Say(BotString.error_exception));
                        break;
                    case CommandError.UnknownCommand:
                        await message.Channel.SendMessageAsync(agent.Say(BotString.error_unknownCommand));
                        break;
                    case CommandError.BadArgCount:
                        await message.Channel.SendMessageAsync(agent.Say(BotString.error_badArgCount));
                        break;
                    default:
                        await message.Channel.SendMessageAsync(result.ErrorReason);
                        break;
                }
                await message.AddReactionAsync(Commands.emoji_error);
            }
        }

        public async Task Start() {
            if (config == null) {
                Log(LogSeverity.Error, "Missing configuration, please fill config.json and try again.");
                await Task.Delay(-1);
            }
            if (config.botToken == null || config.botToken == "") {
                Log(LogSeverity.Error, "Missing bot token, please fill config.json and try again.");
                await Task.Delay(-1);
            }

            switch (discord.LoginState) {
                case LoginState.LoggedIn:
                case LoginState.LoggingIn:
                    await Stop();
                    break;
            }

            // log in & start
            await discord.LoginAsync(TokenType.Bot, config.botToken);
            await discord.StartAsync();
        }

        private async Task Discord_Ready() {
            if (discord.Guilds.Count == 0) {
                Log(LogSeverity.Error, "No guild found! Please invite the bot to a guild first, then restart.");
                Process.Start(string.Format(Commands.link_authorize, discord.CurrentUser.Id));
                await Stop();
                return;
            }

            if (config.autoSpawnEnabled) {
                foreach (var guildId in config.autoSpawnGuildIds) {
                    var guild = discord.GetGuild(guildId);
                    if (guild == null) {
                        Log(LogSeverity.Warning, "No guild with id " + guildId + " to autospawn in found - make sure you have authorized the bot to this guild.");
                        continue;
                    }

                    var owner = config.ownerId != null ? guild.GetUser(config.ownerId.Value) : null;
                    var bot = guild.GetUser(discord.CurrentUser.Id);

                    // choose voice channel
                    IVoiceChannel voiceChannel;
                    if (owner != null && owner.VoiceChannel != null && bot.GetPermissions(owner.VoiceChannel).Connect) {
                        voiceChannel = owner.VoiceChannel;
                    } else if (guild.AFKChannel != null && bot.GetPermissions(guild.AFKChannel).Connect) {
                        voiceChannel = guild.AFKChannel;
                    } else {
                        voiceChannel = guild.VoiceChannels.FirstOrDefault(v => bot.GetPermissions(v).Connect);
                    }

                    if (voiceChannel != null) {
                        await JoinVoice(voiceChannel);
                    } else {
                        Log(LogSeverity.Warning, "No voice channel to autospawn in found - make sure the bot has permissions to connect and the guild has at least one voice channel.");
                    }
                }
            }
        }

        public async Task JoinVoice(IVoiceChannel voiceChannel) {
            VoiceSet voiceSet;
            if (voiceSets.TryGetValue(voiceChannel.GuildId, out voiceSet)) {
                await LeaveVoice(voiceChannel.GuildId);
            } else {
                voiceSet = new VoiceSet();
                voiceSets.TryAdd(voiceChannel.GuildId, voiceSet);
            }

            Log(LogSeverity.Debug, "Spawning");

            // join voice channel
            try {
                voiceSet.audioClient = await voiceChannel.ConnectAsync();
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Failed to connect to voice channel", ex);
                return;
            }

            if (config.speakEnabled) {
                // create speak stream
                voiceSet.speakStream = voiceSet.audioClient.CreatePCMStream(config.speakAudioType, config.speakBitRate ?? voiceChannel.Bitrate, config.speakBufferMillis);

                if (recordChannel == 0) {
                    bool success = true;

                    // start recording
                    try {
                        @startRecording();
                    } catch (Exception ex) {
                        Log(LogSeverity.Error, "Failed to start recording audio", ex);
                        success = false;
                    }

                    if (success) {
                        Log(LogSeverity.Info, "Speaking");
                        string display = config.speakRecordingDevice != "default" ? config.speakRecordingDevice : "Default Recording Device";
                        await discord.SetGameAsync(display, "https://twitch.tv/0", StreamType.Twitch);
                    }
                }
            }

            if (config.listenEnabled) {
                voiceSet.audioClient.StreamCreated += async (userId, listenStream) => listenStreams.TryAdd(userId, listenStream);
                voiceSet.audioClient.StreamDestroyed += async (userId) => { AudioInStream s; listenStreams.TryRemove(userId, out s); };

                bool success = true;

                var users = await voiceChannel.GetUsersAsync().Flatten();
                foreach (var user in users) {
                    if (user.Id != discord.CurrentUser.Id) {
                        listenStreams.TryAdd(user.Id, ((SocketGuildUser)user).AudioStream);
                    }
                }

                Task.Factory.StartNew(pollListen);
                try {
                    startPlaying();
                } catch (Exception ex) {
                    Log(LogSeverity.Error, "Failed to start playing audio", ex);
                    success = false;
                }

                if (success) {
                    Log(LogSeverity.Info, "Listening");
                }
            }
        }

        public bool IsInVoice(ulong guildId) {
            return voiceSets.ContainsKey(guildId);
        }

        private async Task Discord_Log(LogMessage msg) {
            Utils.Log(msg);
        }

        private static void Log(LogSeverity severity, string message, Exception ex = null) {
            Utils.Log(severity, "Bot", message, ex);
        }

        private void startRecording() {
            recordDevice = -1;
            if (config.speakRecordingDevice != null) {
                var devices = Bass.BASS_RecordGetDeviceInfos();
                for (int i = 0; i < devices.Length; i++) {
                    if (devices[i].name == config.speakRecordingDevice) {
                        recordDevice = i;
                        break;
                    }
                }
                if (recordDevice < 0) {
                    IEnumerable<string> devicesList = devices.Select(d => d.name);
                    throw new Exception("Recording device \"" + config.speakRecordingDevice + "\" not found.\nAvailable recording devices:\n * " + string.Join("\n * ", devicesList) + "\n");
                }
            }

            if (Bass.BASS_RecordInit(recordDevice)) {
                recordProc = new RECORDPROC(recordDevice_audioReceived);
                recordChannel = Bass.BASS_RecordStart(frequency, channels, BASSFlag.BASS_RECORD_PAUSE, recordProc, IntPtr.Zero);

                // really start recording
                Bass.BASS_ChannelPlay(recordChannel, false);
            } else {
                throw new Exception("Failed to initialize recording device");
            }
        }

        private void startPlaying() {
            playbackDevice = -1;
            if (config.listenPlaybackDevice != null) {
                var devices = Bass.BASS_GetDeviceInfos();
                for (int i = 0; i < devices.Length; i++) {
                    if (devices[i].name == config.listenPlaybackDevice) {
                        playbackDevice = i;
                        break;
                    }
                }
                if (playbackDevice < 0) {
                    IEnumerable<string> devicesList = devices.Select(d => d.name);
                    throw new Exception("Playback device \"" + config.listenPlaybackDevice + "\" not found.\nAvailable playback devices:\n * " + string.Join("\n * ", devicesList) + "\n");
                }
            }

            if (Bass.BASS_Init(playbackDevice, frequency, BASSInit.BASS_DEVICE_DEFAULT, IntPtr.Zero, null)) {
                playProc = new STREAMPROC(playbackDevice_audioRequested);
                playbackChannel = Bass.BASS_StreamCreate(frequency, channels, BASSFlag.BASS_DEFAULT, playProc, IntPtr.Zero);

                firstPlayProcCall = true;
                // start playing
                Bass.BASS_ChannelPlay(playbackChannel, false);
            } else {
                throw new Exception("Failed to initialize playback device");
            }
        }

        private unsafe bool recordDevice_audioReceived(int handle, IntPtr buffer, int length, IntPtr user) {
            Log(LogSeverity.Debug, "speaking to " + voiceSets.Values.Count + " guilds");
            try {
                foreach (var voiceSet in voiceSets.Values) {
                    lock (voiceSet.speakStream_writeLock) {
                        if (voiceSet.speakStream != null) {
                            using (var data = new UnmanagedMemoryStream((byte*)buffer.ToPointer(), length)) {
                                data.CopyTo(voiceSet.speakStream);
                            }
                        }
                    }
                }
            } catch (OperationCanceledException) {
            }
            return voiceSets.Count > 0;
        }

        private async Task pollListen() {
            playbackBuffer = new MemoryStream();
            var listenStream = listenStreams.Single().Value;
            //var sender = guild.GetUser(listenStreams.Single().Key);
            //bool enabled = !sender.IsMuted && !sender.IsSelfMuted && !sender.IsSuppressed;
            while (true) {
                lock (playbackStream_writeLock) {
                    listenStream.CopyTo(playbackBuffer);
                }
                Console.WriteLine(playbackBuffer.Length + " bytes");
                await Task.Delay(50);
            }
        }

        private unsafe int playbackDevice_audioRequested(int handle, IntPtr buffer, int length, IntPtr user) {
            if (firstPlayProcCall) {
                // first call is synchronous, following calls are asynchronous.
                firstPlayProcCall = false;
                return 0;
            }

            try {
                int bytesread = (int)playbackBuffer.Length;
                Marshal.Copy(playbackBuffer.GetBuffer(), 0, buffer, bytesread);
                return bytesread;
            } catch (OperationCanceledException) {
                if (playbackBuffer != null) {
                    playbackBuffer.Close();
                }
                return (int)BASSStreamProc.BASS_STREAMPROC_END;
            }
        }

        public async Task LeaveVoice(ulong guildId) {
            VoiceSet voiceSet;
            if (!voiceSets.TryRemove(guildId, out voiceSet)) {
                return;
            }

            Log(LogSeverity.Debug, "Despawning");

            if (voiceSets.Count == 0) {
                // stop playing
                if (playbackChannel != 0) {
                    Bass.BASS_ChannelStop(playbackChannel);
                    Bass.BASS_StreamFree(playbackChannel);
                    playbackChannel = 0;
                }
                Bass.BASS_Free();
            }

            // leave voice
            if (voiceSet.speakStream != null) {
                voiceSet.speakStream.Close();
                voiceSet.speakStream = null;
            }
            if (voiceSet.audioClient != null) {
                await voiceSet.audioClient.StopAsync();
            }

            if (voiceSets.Count == 0) {
                await discord.SetGameAsync("");
            }
        }

        public async Task Stop() {
            Log(LogSeverity.Info, "Stopping");

            foreach (var guildId in voiceSets.Keys) {
                await LeaveVoice(guildId);
            }

            // stop discord client
            if (discord != null) {
                await discord.StopAsync();
                // Wait a little for the client to finish disconnecting before allowing the program to return
                await Task.Delay(500);
            }
        }

        private bool consoleCtrl(WinApi.CtrlTypes type) {
            switch (type) {
                case WinApi.CtrlTypes.CTRL_BREAK_EVENT:
                case WinApi.CtrlTypes.CTRL_CLOSE_EVENT:
                case WinApi.CtrlTypes.CTRL_C_EVENT:
                case WinApi.CtrlTypes.CTRL_LOGOFF_EVENT:
                case WinApi.CtrlTypes.CTRL_SHUTDOWN_EVENT:
                default:
                    Stop().GetAwaiter().GetResult();
                    Dispose();
                    break;
            }
            return true;
        }

        public void Dispose() {
            /*if(discord != null) {
                try {
                    discord.Dispose();
                } catch {
                }
                discord = null;
            }*/
        }
    }
}
