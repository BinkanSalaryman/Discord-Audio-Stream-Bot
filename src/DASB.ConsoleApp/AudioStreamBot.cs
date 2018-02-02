using DASB.Properties;
using Discord;
using Discord.Audio;
using Discord.Commands;
using Discord.Net;
using Discord.Rest;
using Discord.WebSocket;
using Microsoft.Extensions.DependencyInjection;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using Un4seen.Bass;

namespace DASB {
    public class AudioStreamBot : IDisposable {
        public const int SAMPLE_RATE = 48000;
        public const int SAMPLE_SIZE = sizeof(short);
        public const int CHANNEL_COUNT = 2;
        public const int FRAME_SAMPLES = 20 * (SAMPLE_RATE / 1000);

        public static IServiceProvider Services { get; private set; }
        private static IServiceCollection _services = new ServiceCollection();

        public static void ModifyServices(Action<IServiceCollection> modify) {
            modify(_services);
            Services = _services.BuildServiceProvider();
        }

        static AudioStreamBot() {
            AppDomain.CurrentDomain.UnhandledException += CurrentDomain_UnhandledException;
            CultureInfo.DefaultThreadCurrentCulture = CultureInfo.InvariantCulture;
            CultureInfo.DefaultThreadCurrentUICulture = CultureInfo.InvariantCulture;
        }

        // general
        public Config Config { get; private set; }
        public BotAgent Agent { get; private set; }
        public DiscordSocketClient Discord { get; private set; }
        public RestApplication AppInfo { get; private set; }
        public Assembly CommandsAssembly { get; private set; }

        private string configPath;
        private string agentsPath;
        private string commandsPath;
        private CommandService commandService;
        private WinApi.HandlerRoutine consoleCtrlHandler;

        // voice
        private ConcurrentDictionary<ulong, VoiceSet> voiceSets = new ConcurrentDictionary<ulong, VoiceSet>();

        private readonly object record_lock = new object();
        private int recordDevice;
        private int recordChannel;
        private RECORDPROC recordProc;

        private readonly object playback_lock = new object();
        private int playbackDevice;
        private int playbackChannel;
        private STREAMPROC playProc;
        private bool firstPlayProcCall;
        private MemoryQueue playbackQueue;

        private static void Main(params string[] args) {
            MainAsync(args).GetAwaiter().GetResult();
        }

        private static async Task MainAsync(params string[] args) {
            Console.Title = Assembly.GetExecutingAssembly().GetName().Name;
            BassNet.Registration("poo@poo.com", "2X25242411252422");

            string configPath;
            switch (args.Length) {
                case 0:
                    configPath = Path.Combine(Directory.GetParent(Assembly.GetExecutingAssembly().Location).FullName, "config.json");
                    break;
                case 1:
                default:
                    configPath = args[0];
                    break;
            }

            string agentsPath = Path.Combine(Directory.GetParent(Assembly.GetExecutingAssembly().Location).FullName, "agents");
            string commandsPath = Path.Combine(Directory.GetParent(Assembly.GetExecutingAssembly().Location).FullName, "commands.dll");

            var bot = new AudioStreamBot();
            try {
                if (await bot.Init(configPath, agentsPath, commandsPath)) {
                    await bot.Start();
                    await Task.Delay(-1);
                }
            } finally {
                bot.Dispose();
            }

            Console.WriteLine("Press any key . . .");
            Console.ReadKey();
        }

        private static void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e) {
            Log(LogSeverity.Critical, "An unhandled exception has occured", e.ExceptionObject as Exception);
            Console.WriteLine("Press any key . . .");
            Console.ReadKey();
            Environment.Exit(0);
        }

        private async Task<bool> Init(string configPath, string agentsPath, string commandsPath) {
            this.configPath = configPath;
            this.agentsPath = agentsPath;
            this.commandsPath = commandsPath;

            // create discord client
            Discord = new DiscordSocketClient();
            Discord.JoinedGuild += Discord_JoinedGuild;
            Discord.LeftGuild += Discord_LeftGuild;
            Discord.MessageReceived += Discord_MessageReceived;
            Discord.Log += Log;
            Discord.Ready += Discord_Ready;

            commandService = new CommandService();
            commandService.Log += Log;
            try {
                CommandsAssembly = Assembly.LoadFile(Path.GetFullPath(commandsPath));
                await commandService.AddModulesAsync(CommandsAssembly);
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Failed to load commands assembly at \"" + commandsPath + "\"", ex);
                return false;
            }

            // install commands
            ModifyServices(x => x
                .AddSingleton(Discord)
                .AddSingleton(commandService)
            );

            // load config
            if (!ReloadConfig()) {
                return false;
            }

            // export embedded bot agent as reference
            if (!Directory.Exists(agentsPath)) {
                Directory.CreateDirectory(agentsPath);
            }
            File.WriteAllBytes(Path.Combine(agentsPath, "tomoko.json"), Resources.tomoko);

            // load agent
            if (!ReloadAgent()) {
                return false;
            }

            // capture console exit
            consoleCtrlHandler = new WinApi.HandlerRoutine(consoleCtrl);
            WinApi.SetConsoleCtrlHandler(consoleCtrlHandler, true);

            // init record device
            if (Config.speakEnabled) {
                recordDevice = -1;
                if (Config.speakRecordingDevice != null) {
                    var devices = Bass.BASS_RecordGetDeviceInfos();
                    for (int i = 0; i < devices.Length; i++) {
                        if (devices[i].name == Config.speakRecordingDevice) {
                            recordDevice = i;
                            break;
                        }
                    }
                    if (recordDevice < 0) {
                        IEnumerable<string> devicesList = devices.Select(d => d.name);
                        Log(LogSeverity.Error, "Recording device \"" + Config.speakRecordingDevice + "\" not found.\nAvailable recording devices:\n * " + string.Join("\n * ", devicesList) + "\n");
                        return false;
                    }
                }

                if (!Bass.BASS_RecordInit(recordDevice)) {
                    Log(LogSeverity.Error, "Failed to init recording device: " + Bass.BASS_ErrorGetCode());
                    return false;
                }
                recordProc = new RECORDPROC(recordDevice_audioReceived);
            }

            // init playback device
            if (Config.listenEnabled) {
                playbackDevice = -1;
                if (Config.listenPlaybackDevice != null) {
                    var devices = Bass.BASS_GetDeviceInfos();
                    for (int i = 0; i < devices.Length; i++) {
                        if (devices[i].name == Config.listenPlaybackDevice) {
                            playbackDevice = i;
                            break;
                        }
                    }
                    if (playbackDevice < 0) {
                        IEnumerable<string> devicesList = devices.Select(d => d.name);
                        Log(LogSeverity.Error, "Playback device \"" + Config.listenPlaybackDevice + "\" not found.\nAvailable playback devices:\n * " + string.Join("\n * ", devicesList) + "\n");
                        return false;
                    }
                }

                if (!Bass.BASS_Init(playbackDevice, SAMPLE_RATE, BASSInit.BASS_DEVICE_DEFAULT, IntPtr.Zero, null)) {
                    Log(LogSeverity.Error, "Failed to init playback device: " + Bass.BASS_ErrorGetCode());
                    return false;
                }
                playProc = new STREAMPROC(playbackDevice_audioRequested);
            }

            return true;
        }

        private async Task Discord_MessageReceived(SocketMessage messageParam) {
            var message = messageParam as SocketUserMessage;
            if (message == null || message.Author.Id == Discord.CurrentUser.Id) {
                return;
            }

            int argPos = 0;
            bool isCommand = message.HasMentionPrefix(Discord.CurrentUser, ref argPos) || message.Channel is IDMChannel;
            if (!isCommand) {
                return;
            }

            Log(LogSeverity.Verbose, message.Author.Username + "#" + message.Author.Discriminator + ": " + message.Content.Substring(argPos));

            Task.Run(async () => await RunCommand(message, argPos));
        }

        public async Task RunCommand(SocketUserMessage message, int argPos, SocketGuild guild = null) {
            // run command
            var context = new CommandContext(this, message, guild ?? (message.Channel as SocketGuildChannel)?.Guild);
            IResult result = await commandService.ExecuteAsync(context, argPos, Services);

            if (!result.IsSuccess) {
                switch (result.Error) {
                    case CommandError.Exception:
                        switch (result.ErrorReason) {
                            case nameof(RequireGuildContextAttribute):
                                await message.Channel.SendMessageAsync(Agent.Say(BotString.warning_guildContextRequired));
                                break;
                            case nameof(DisablePermissionCheckAttribute):
                                await message.Channel.SendMessageAsync(Agent.Say(BotString.warning_permissionsRequired));
                                break;
                            default:
                                Log(LogSeverity.Error, "Command error: " + result.ErrorReason);
                                await message.Channel.SendMessageAsync(Agent.Say(BotString.error_exception));
                                break;
                        }
                        break;
                    case CommandError.UnknownCommand:
                        await message.Channel.SendMessageAsync(Agent.Say(BotString.error_unknownCommand));
                        break;
                    case CommandError.BadArgCount:
                        await message.Channel.SendMessageAsync(Agent.Say(BotString.error_badArgCount));
                        break;
                    default:
                        await message.Channel.SendMessageAsync(Agent.Say(BotString.error_reason, result.ErrorReason));
                        break;
                }
            }
        }

        public async Task Start() {
            if (Config == null) {
                Log(LogSeverity.Error, "Missing configuration, please fill config.json and try again.");
                await Task.Delay(-1);
            }
            if (Config.botToken == null) {
                Log(LogSeverity.Warning, "No bot token is set! Please create an application, create a bot user; then update the configuration file / enter data in the console.");
                queryBotToken();
            }

            switch (Discord.LoginState) {
                case LoginState.LoggedIn:
                case LoginState.LoggingIn:
                    await Stop();
                    break;
            }

            // log in & start
            do {
                try {
                    await Discord.LoginAsync(TokenType.Bot, Config.botToken);
                    await Discord.StartAsync();
                } catch (HttpException ex) when (ex.HttpCode == HttpStatusCode.Unauthorized) {
                    Log(LogSeverity.Error, "Bot token is invalid", ex);
                    queryBotToken();
                    continue;
                } catch (Exception ex) {
                    Log(LogSeverity.Error, "Login failed.", ex);
                    continue;
                }
            } while (false);
        }

        private async Task Discord_Ready() {
            Console.Title = Assembly.GetExecutingAssembly().GetName().Name + " (" + Discord.CurrentUser.Username + "#" + Discord.CurrentUser.Discriminator + ")";
            AppInfo = await Discord.GetApplicationInfoAsync();

            if (Discord.Guilds.Count == 0) {
                showNoGuildFound();
            }
        }

        private async Task Discord_JoinedGuild(SocketGuild guild) {
            if (Discord.Guilds.Count == 1) {

            }
        }

        private async Task Discord_LeftGuild(SocketGuild guild) {
            if (Discord.Guilds.Count == 0) {
                showNoGuildFound();
            }
        }

        private void queryBotToken() {
            Process.Start(Utils.link_discordApplications);

            Console.ForegroundColor = ConsoleColor.White;
            Console.WriteLine("Please enter bot token (My Apps > New App > APP BOT USER > Token):");

            Console.ForegroundColor = ConsoleColor.Green;
            Console.Write("$ ");
            Console.ForegroundColor = ConsoleColor.White;
            Config.botToken = Console.ReadLine();
            UpdateConfig();
        }

        public bool ReloadConfig() {
            if (File.Exists(configPath)) {
                try {
                    Config = new Config(File.ReadAllText(configPath));
                } catch (Exception ex) {
                    Log(LogSeverity.Error, "Failed to parse configuration", ex);
                    return false;
                }
            } else {
                Log(LogSeverity.Info, "Configuration file not found, writing prototype to " + configPath);
                Config = new Config();
                // fill default default permissions
                foreach (var command in commandService.Commands) {
                    var dp = command.Attributes.OfType<DefaultPermissionAttribute>().SingleOrDefault();
                    if (dp != null) {
                        switch (dp.DefaultPermission) {
                            case Permission.Accept:
                                Config.commandsDefaultPermissions.Add(Utils.GetCommandKey(command));
                                break;
                            case Permission.Reject:
                                Config.commandsDefaultPermissions.Add("!" + Utils.GetCommandKey(command));
                                break;
                        }
                    }
                }
            }
            File.WriteAllText(configPath, Config.ToString());
            return true;
        }

        public bool UpdateConfig() {
            try {
                File.WriteAllText(configPath, Config.ToString());
                return true;
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Failed to save file at \"" + configPath + "\"", ex);
                return false;
            }
        }

        public bool ReloadAgent() {
            string filename = Path.HasExtension(Config.commandsBotAgent) ? Config.commandsBotAgent : Path.ChangeExtension(Config.commandsBotAgent, "json");
            string path = Path.Combine(agentsPath, filename);

            try {
                Agent = new BotAgent(File.ReadAllText(path));
                return true;
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Failed to load bot agent file at \"" + path + "\"", ex);
                return false;
            }
        }

        private void showNoGuildFound() {
            Log(LogSeverity.Warning, "No guild found! Please authorize/invite the bot to a guild.");
            Process.Start(string.Format(Utils.link_discordAuthorize, Discord.CurrentUser.Id));
        }

        public async Task JoinVoice(SocketVoiceChannel voiceChannel) {
            VoiceSet voiceSet;
            if (voiceSets.TryGetValue(voiceChannel.Guild.Id, out voiceSet)) {
                await LeaveVoice(voiceChannel.Guild.Id);
            }
            voiceSet = new VoiceSet();
            voiceSets.TryAdd(voiceChannel.Guild.Id, voiceSet);

            // join voice channel
            try {
                voiceSet.audioClient = await voiceChannel.ConnectAsync();
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Failed to connect to voice channel", ex);
                return;
            }

            if (Config.speakEnabled) {
                // create speak stream
                voiceSet.speakStream = voiceSet.audioClient.CreatePCMStream(Config.speakAudioType, Config.speakBitRate ?? voiceChannel.Bitrate, Config.speakBufferMillis);

                // start recording
                if (recordChannel == 0 || Bass.BASS_ChannelIsActive(recordChannel) != BASSActive.BASS_ACTIVE_PLAYING) {
                    if (recordChannel == 0) {
                        recordChannel = Bass.BASS_RecordStart(SAMPLE_RATE, CHANNEL_COUNT, BASSFlag.BASS_RECORD_PAUSE, recordProc, IntPtr.Zero);
                    }
                    Bass.BASS_ChannelPlay(recordChannel, false);

                    await Discord.SetGameAsync(Config.speakRecordingDevice ?? "Default Recording Device", Utils.link_twitchDummyStream, StreamType.Twitch);
                }
            }

            if (Config.listenEnabled) {
                // create listen streams
                foreach (var user in voiceChannel.Users) {
                    voiceSet.listenStreams.TryAdd(user.Id, user.AudioStream);
                }
                voiceSet.audioClient.StreamCreated += async (userId, listenStream) => voiceSet.listenStreams.TryAdd(userId, listenStream);
                voiceSet.audioClient.StreamDestroyed += async (userId) => { AudioInStream s; voiceSet.listenStreams.TryRemove(userId, out s); };

                // start playback
                if (playbackChannel == 0 || Bass.BASS_ChannelIsActive(playbackChannel) != BASSActive.BASS_ACTIVE_PLAYING) {
                    if (playbackChannel == 0) {
                        playbackChannel = Bass.BASS_StreamCreate(SAMPLE_RATE, CHANNEL_COUNT, BASSFlag.BASS_DEFAULT, playProc, IntPtr.Zero);
                    }
                    firstPlayProcCall = true;
                    Bass.BASS_ChannelPlay(playbackChannel, false);
                }
            }
        }

        public bool IsInVoice(ulong guildId) {
            return voiceSets.ContainsKey(guildId);
        }

        private async Task Log(LogMessage message) {
            Utils.Log(message);
        }

        private static void Log(LogSeverity severity, string message, Exception ex = null) {
            Utils.Log(severity, "Bot", message, ex);
        }

        private bool recordDevice_audioReceived(int handle, IntPtr buffer, int length, IntPtr user) {
            try {
                lock (record_lock) {
                    foreach (var voiceSet in voiceSets) {
                        if (voiceSet.Value.speakStream == null) {
                            continue;
                        }

                        var self = Discord.GetGuild(voiceSet.Key).GetUser(Discord.CurrentUser.Id);
                        if (self == null) {
                            continue;
                        }

                        // send audio to discord voice
                        using (var stream = Utils.OpenBuffer(buffer, length, FileAccess.Read)) {
                            stream.CopyTo(voiceSet.Value.speakStream);
                        }
                    }
                    return true;
                }
            } catch (OperationCanceledException) {
                Log(LogSeverity.Debug, "Audio recording canceled");
                return false;
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Error in audio recording", ex);
                return false;
            }
        }

        private int playbackDevice_audioRequested(int handle, IntPtr buffer, int length, IntPtr user) {
            if (firstPlayProcCall) {
                // first call is synchronous, following calls are asynchronous.
                firstPlayProcCall = false;
                if (playbackQueue == null) {
                    playbackQueue = new MemoryQueue();
                } else {
                    playbackQueue.Clear();
                }
                return 0;
            }

            try {
                lock (playback_lock) {
                    // read audio from users we're listening to
                    var frames = new List<RTPFrame>();
                    foreach (var voiceSet in voiceSets) {
                        var guild = Discord.GetGuild(voiceSet.Key);
                        foreach (var listenStream in voiceSet.Value.listenStreams) {
                            if (listenStream.Value == null) {
                                continue;
                            }

                            var sender = guild.GetUser(listenStream.Key);
                            if (sender == null || sender.IsMuted || sender.IsSelfMuted || sender.IsSuppressed) {
                                continue;
                            }

                            Log(LogSeverity.Debug, "listen:" + sender.Nickname + " frames[" + listenStream.Value.AvailableFrames + "]");
                            for (int f = 0; f < listenStream.Value.AvailableFrames; f++) {
                                var frame = listenStream.Value.ReadFrameAsync(CancellationToken.None).GetAwaiter().GetResult();
                                if (frame.Missed) {
                                    Log(LogSeverity.Debug, "RTP frame missed");
                                }
                                frames.Add(frame);
                            }
                        }
                    }

                    // mix audio
                    frames.Sort((o1, o2) => (int)(o1.Timestamp - o2.Timestamp));
                    using (var stream = playbackQueue.AsStream(FileAccess.Write)) {
                        mixRTPFrames(frames, stream);
                    }

                    // send audio to playback device
                    using (var stream = Utils.OpenBuffer(buffer, length, FileAccess.Write)) {
                        playbackQueue.Dequeue(stream, Math.Min(Math.Max(0, playbackQueue.Length), length));
                        return (int)stream.Position;
                    }
                }
            } catch (OperationCanceledException) {
                Log(LogSeverity.Debug, "Audio playback canceled");
                return (int)BASSStreamProc.BASS_STREAMPROC_END;
            } catch (Exception ex) {
                Log(LogSeverity.Error, "Error in audio playback", ex);
                return (int)BASSStreamProc.BASS_STREAMPROC_END;
            }
        }

        private static void mixRTPFrames(IReadOnlyList<RTPFrame> sortedFrames, Stream stream) {
            if (sortedFrames.Count == 0) {
                return;
            }

            uint startTimestamp = sortedFrames[0].Timestamp;
            uint endTimestamp = sortedFrames[sortedFrames.Count - 1].Timestamp;

            byte[] sampleBuffer = new byte[SAMPLE_SIZE];
            for (int f = 0; f <= (endTimestamp - startTimestamp) / FRAME_SAMPLES; f++) {
                for (int s = 0; s < FRAME_SAMPLES; s++) {
                    int sample = 0;

                    foreach (var frame in sortedFrames) {
                        if (!frame.Missed && (frame.Timestamp - startTimestamp) / FRAME_SAMPLES == f) {
                            sample += Utils.GetInt16(frame.Payload, s * SAMPLE_SIZE, false);
                        }
                    }

                    Utils.GetBytes((short)Math.Min(Math.Max(short.MinValue, sample), short.MaxValue), sampleBuffer, 0, false);
                    stream.Write(sampleBuffer, 0, SAMPLE_SIZE);
                }
            }
        }

        public async Task LeaveVoice(ulong guildId) {
            VoiceSet voiceSet;
            if (!voiceSets.TryRemove(guildId, out voiceSet)) {
                return;
            }

            // disconnect audio streams
            if (voiceSet.speakStream != null) {
                voiceSet.speakStream.Close();
            }
            foreach (var listenStream in voiceSet.listenStreams) {
                if (listenStream.Value != null) {
                    listenStream.Value.Close();
                }
            }

            // leave voice chat
            await voiceSet.audioClient.StopAsync();
            voiceSet.audioClient.Dispose();

            if (voiceSets.Count == 0) {
                // stop recording
                if (recordChannel != 0 && Bass.BASS_ChannelIsActive(recordChannel) != BASSActive.BASS_ACTIVE_STOPPED) {
                    Bass.BASS_ChannelStop(recordChannel);
                    Bass.BASS_StreamFree(recordChannel);
                    recordChannel = 0;
                }
                await Discord.SetGameAsync("", Utils.link_twitchDummyStream, StreamType.Twitch);

                // stop playback
                if (playbackChannel != 0 && Bass.BASS_ChannelIsActive(playbackChannel) != BASSActive.BASS_ACTIVE_STOPPED) {
                    Bass.BASS_ChannelStop(playbackChannel);
                    Bass.BASS_StreamFree(playbackChannel);
                    playbackChannel = 0;
                }
            }
        }

        public async Task LeaveVoiceAll() {
            foreach (var guildId in voiceSets.Keys) {
                await LeaveVoice(guildId);
            }
        }

        public async Task Stop() {
            Log(LogSeverity.Info, "Stopping");

            await LeaveVoiceAll();

            // stop discord client
            await Discord.StopAsync();
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
            if (Discord != null) {
                try {
                    Discord.Dispose();
                } catch {
                }
                Discord = null;
            }
            if (recordChannel != 0) {
                Bass.BASS_StreamFree(recordChannel);
            }
            if (playbackChannel != 0) {
                Bass.BASS_StreamFree(playbackChannel);
            }
            Bass.BASS_Free();
            Bass.BASS_RecordFree();
        }
    }
}
