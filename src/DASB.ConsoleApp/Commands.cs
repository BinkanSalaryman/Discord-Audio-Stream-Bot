using Discord;
using Discord.Commands;
using Discord.WebSocket;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.NetworkInformation;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    [Group]
    public class Commands : ModuleBase<AudioStreamBot.CommandContext> {
        public static Emoji emoji_success => new Emoji(char.ConvertFromUtf32(0x2705));
        public static Emoji emoji_forbidden => new Emoji(char.ConvertFromUtf32(0x26d4));
        public static Emoji emoji_warning => new Emoji(char.ConvertFromUtf32(0x26a0));
        public static Emoji emoji_error => new Emoji(char.ConvertFromUtf32(0x0001f916));
        public static Emoji emoji_exit => new Emoji(char.ConvertFromUtf32(0x0001f44b));
        
        public const string link_applications = "https://discordapp.com/developers/applications/me";
        public const string link_authorize = "https://discordapp.com/oauth2/authorize?client_id={0}&scope=bot&permissions=0";

        public const string setStreaming = "streaming";
        public const string setPlaying = "playing";
        public const string setStatus = "status";
        public const string setNickname = "nickname";
        public const string sendMail = "whisper";
        public const string showApplicationsLink = "applications";
        public const string showAuthorizeLink = "invite";
        public const string stop = "stop";
        public const string joinVoice = "spawn";
        public const string leaveVoice = "despawn";
        public const string help = "help";
        public const string listCommands = "commands";
        public const string assignPermissions = "assign";
        public const string listPermissions = "permissions";
        public const string ping = "ping";

        [Command(setStreaming)]
        public async Task SetStreamingAsync([Remainder] string game = "") {
            if (!checkRun()) return;

            await Context.Discord.SetGameAsync(game, "http://twitch.tv/0", StreamType.Twitch);
        }

        [Command(setPlaying)]
        public async Task SetPlayingAsync([Remainder] string game = "") {
            if (!checkRun()) return;

            await Context.Discord.SetGameAsync(game);
        }

        [Command(setStatus)]
        public async Task SetStatusAsync([Remainder] string status = null) {
            if (!checkRun()) return;

            await Context.Discord.SetStatusAsync(Utils.ParseUserStatus(status ?? "online"));
        }

        [Command(setNickname)]
        public async Task SetNicknameAsync([Remainder] string nick = "") {
            if (!checkRun()) return;
            if (!checkGuildContext()) return;

            await Context.Guild.GetUser(Context.Discord.CurrentUser.Id).ModifyAsync(x => x.Nickname = nick);
        }

        [Command(sendMail)]
        public async Task SendMailAsync(string recipient, [Remainder] string message) {
            if (!checkRun()) return;

            var users = Utils.ParseUsers(Context.Discord, recipient, Context.Guild);
            if (users == null) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_badUserId));
                return;
            }
            foreach (var user in users) {
                var channel = await user.GetOrCreateDMChannelAsync();

                StringBuilder content = new StringBuilder();
                content.Append(say(BotString.info_mailHead, user.Mention));
                content.AppendLine(message);
                await channel.SendMessageAsync(content.ToString());
            }
        }

        [Command(showApplicationsLink)]
        public async Task ShowApplicationsLinkAsyc() {
            if (!checkRun()) return;

            await ReplyAsync(string.Format(link_applications, Context.Discord.CurrentUser.Id));
        }

        [Command(showAuthorizeLink)]
        public async Task ShowInviteLinkAsync() {
            if (!checkRun()) return;

            await ReplyAsync(string.Format(link_authorize, Context.Discord.CurrentUser.Id));
        }

        [Command(stop)]
        public async Task StopAsync() {
            if (!checkRun()) return;

            await Context.Message.AddReactionAsync(emoji_exit);
            await Context.Bot.Stop();
            Context.Bot.Dispose();
            Environment.Exit(0);
        }

        [Command(joinVoice)]
        public async Task JoinVoiceAsync([Remainder] string channel = null) {
            if (!checkRun()) return;
            if (!checkGuildContext()) return;

            // find bot user in guild
            SocketGuildUser self = Context.Guild.GetUser(Context.Discord.CurrentUser.Id);
            if (self == null) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_notInGuildSelf));
                return;
            }

            if (channel != null) {
                // find voice channel
                SocketVoiceChannel channel_;
                ulong channelId;
                if (ulong.TryParse(channel, out channelId)) {
                    channel_ = Context.Guild.GetVoiceChannel(channelId);
                    if (channel_ == null) {
                        Context.Feedback = CommandFeedback.Warning;
                        await ReplyAsync(say(BotString.warning_badVoiceChannelId));
                        return;
                    }
                } else {
                    var channels = Context.Guild.VoiceChannels.Where(v => v.Name == channel).ToList();
                    if (channels.Count == 0) {
                        Context.Feedback = CommandFeedback.Warning;
                        await ReplyAsync(say(BotString.warning_voiceChannelNameNotFound));
                        return;
                    }
                    if (channels.Count > 1) {
                        Context.Feedback = CommandFeedback.Warning;
                        await ReplyAsync(say(BotString.warning_voiceChannelNameAmbigous));
                        return;
                    }
                    channel_ = channels[0];
                }
                if (!self.GetPermissions(channel_).Connect) {
                    Context.Feedback = CommandFeedback.Warning;
                    await ReplyAsync(say(BotString.warning_noPermissionConnect_specifiedChannel));
                    return;
                }

                // connect to specified voice channel
                await Context.Bot.JoinVoice(channel_);
            } else {
                SocketVoiceChannel channel_ = null;

                // find author in guild
                SocketGuildUser author = Context.Guild.GetUser(Context.Message.Author.Id);
                if (author != null) {
                    channel_ = author.VoiceChannel;
                } else {
                    // woops
                    Context.Feedback = CommandFeedback.Warning;
                    await ReplyAsync(say(BotString.warning_notInGuildAuthor));
                }

                if (channel_ != null) {
                    if (!self.GetPermissions(channel_).Connect) {
                        // missing permissions
                        Context.Feedback = CommandFeedback.Warning;
                        await ReplyAsync(say(BotString.warning_noPermissionConnect_authorsChannel));
                    } else {
                        goto L_spawn;
                    }
                }
                if (channel_ == null) {
                    // try to connect to AFK channel
                    if (Context.Guild.AFKChannel != null && self.GetPermissions(Context.Guild.AFKChannel).Connect) {
                        channel_ = Context.Guild.AFKChannel;
                        goto L_spawn;
                    }
                }
                if (channel_ == null) {
                    // try to connect to any channel
                    channel_ = Context.Guild.VoiceChannels.FirstOrDefault(x => self.GetPermissions(x).Connect);
                }

                L_spawn:
                if (channel_ != null) {
                    // connect to voice channel
                    await Context.Bot.JoinVoice(channel_);
                }
            }
        }

        [Command(leaveVoice)]
        public async Task LeaveVoiceAsync() {
            if (!checkRun()) return;
            if (!checkGuildContext()) return;

            if (!Context.Bot.IsInVoice(Context.Guild.Id)) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_notInVoice));
                return;
            }
            await Context.Bot.LeaveVoice(Context.Guild.Id);
        }

        [Command(help)]
        public async Task HelpAsync() {
            if (!checkRun()) return;

            StringBuilder reply = new StringBuilder();
            reply.AppendLine("Use `commands` to show available commands.");
            reply.AppendLine("Use `help <command>` to get further help.");
            await ReplyAsync(reply.ToString());
        }

        [Command(help)]
        public async Task HelpAsync(string command) {
            if (!checkRun()) return;

            bool success = false;
            foreach (var method in typeof(Commands).GetMethods()) {
                CommandAttribute cmd = (CommandAttribute)method.GetCustomAttributes(typeof(CommandAttribute), false).FirstOrDefault();
                if (cmd != null && cmd.Text == command) {
                    success = true;
                    StringBuilder reply = new StringBuilder();
                    reply.AppendLine("**Syntax:** `" + cmd.Text + " " + string.Join(" ", method.GetParameters().Select(p => "<" + p.Name + ">")) + "`");
                    string help = Context.Agent.Help(cmd, method.GetParameters().Length);
                    if (help != null) {
                        reply.AppendLine("```" + help + "```");
                    }
                    await ReplyAsync(reply.ToString());
                }
            }
            if (!success) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_unknownCommand, command));
                return;
            }
        }

        [Command(listCommands)]
        public async Task ListCommandsAsync() {
            if (!checkRun()) return;

            var commands = new List<CommandAttribute>();
            foreach (var method in GetType().GetMethods()) {
                CommandAttribute cmd = (CommandAttribute)method.GetCustomAttributes(typeof(CommandAttribute), false).FirstOrDefault();
                if (cmd != null) {
                    commands.Add(cmd);
                }
            }

            StringBuilder reply = new StringBuilder();
            reply.AppendLine("**Available commands:**");
            reply.Append(string.Join("・", commands.Select(c => c.Text).OrderBy(c => c).Distinct().Select(c => "`" + c + "`")));
            reply.AppendLine(".");
            await ReplyAsync(reply.ToString());
        }

        [Command(assignPermissions)]
        public async Task AssignPermissionsAsync(string user, [Remainder] string commands) {
            if (!checkRun()) return;

            var user_ = Utils.ParseUser(Context.Discord, user);
            if (user == null) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_badUserId));
                return;
            }
            foreach (var command in commands.Split(' ')) {
                Context.Config.commandsUserPermissions.Assign(user_.Id, command);
            }
            Context.SaveConfig();
        }

        [Command(listPermissions)]
        public async Task ListPermissionsAsync([Remainder] string user = "") {
            if (!checkRun()) return;

            SocketUser user_;
            StringBuilder reply = new StringBuilder();
            if (user == "") {
                user_ = Context.Message.Author;
                reply.AppendLine(say(BotString.info_permissionsHeadSelf));
            } else {
                user_ = Utils.ParseUser(Context.Discord, user);
                reply.Append(say(BotString.info_permissionsHeadOther, user_.Mention));
            }

            if (Context.Config.commandsUserPermissions.ContainsKey(user_.Id)) {
                reply.Append(string.Join("・", Context.Config.commandsUserPermissions[user_.Id].OrderBy(g => g).Select(c => c.StartsWith("!") ? "!`" + c.Substring(1) + "`" : "`" + c + "`")));
            }
            if (Context.Config.ownerId.HasValue && Context.Config.ownerId == user_.Id) {
                reply.Append(" (owner)");
            }
            reply.AppendLine(".");
            await ReplyAsync(reply.ToString());
        }

        [Command(ping)]
        public async Task PingAsync([Remainder] string host = "") {
            if (!checkRun()) return;

            if (host == "") {
                await ReplyAsync(say(BotString.info_pingSelf, Context.Discord.Latency));
            } else {
                try {
                    Ping ping = new Ping();
                    var reply = await ping.SendPingAsync(host);
                    if (reply.Status == IPStatus.Success) {
                        await ReplyAsync(say(BotString.info_pingSuccess, reply.RoundtripTime));
                    }
                } catch (PingException) {
                    await ReplyAsync(say(BotString.info_pingFailed));
                }
            }
        }

        private string say(BotString @string) {
            return Context.Agent.Say(@string);
        }

        private string say(BotString @string, params object[] args) {
            return Context.Agent.Say(@string, args);
        }

        private bool checkRun(int stackOffset = 0) {
            const int stackIndex = 3;
            var stackTrace = new StackTrace();
            var commandMethod = stackTrace.GetFrame(stackIndex + stackOffset).GetMethod();
            CommandAttribute cmd = (CommandAttribute)commandMethod.GetCustomAttributes(typeof(CommandAttribute), false).Single();
            return checkRun(cmd.Text);
        }

        private bool checkRun(string command) {
            bool result = canRun(Context.Message.Author.Id, command);
            if (!result) {
                Context.Feedback = CommandFeedback.NoEntry;
            }
            return result;
        }

        private bool canRun(ulong userId, string command) {
            return (Context.Config.ownerId.HasValue && userId == Context.Config.ownerId) || Context.Config.commandsUserPermissions.Check(userId, command, x => Context.Config.commandsDefaultPermissions.Contains(x));
        }

        private bool checkGuildContext() {
            bool result = Context.Guild != null;
            if (!result) {
                Context.Feedback = CommandFeedback.GuildContextRequired;
            }
            return result;
        }
    }
}
