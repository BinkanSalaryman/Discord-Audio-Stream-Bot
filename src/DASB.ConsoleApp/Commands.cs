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
        // bot user
        public const string setStreaming = "streaming";
        public const string setPlaying = "playing";
        public const string setStatus = "status";
        public const string setNickname = "nickname";
        public const string joinVoice = "spawn";
        public const string leaveVoice = "despawn";

        // bot program
        public const string stop = "stop";
        public const string help = "help";
        public const string listCommands = "commands";
        public const string listPermissions = "permissions";
        public const string assignPermissions = "assign";

        // other
        public const string showAuthorizeLink = "invite";
        public const string showApplicationsLink = "applications";
        public const string ping = "ping";
        public const string sendMail = "whisper";

        #region bot user
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

            var status_ = Utils.ParseUserStatus(status ?? "online");
            if (!status_.HasValue) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_badStatus));
                return;
            }
            await Context.Discord.SetStatusAsync(status_.Value);
        }

        [Command(setNickname)]
        public async Task SetNicknameAsync([Remainder] string nick = "") {
            if (!checkRun()) return;
            if (!checkGuildContext()) return;

            await Context.Guild.GetUser(Context.Discord.CurrentUser.Id).ModifyAsync(x => x.Nickname = nick);
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
        #endregion

        #region bot program
        [Command(stop)]
        public async Task StopAsync() {
            if (!checkRun()) return;

            await Context.Message.AddReactionAsync(Utils.emoji_exit);
            await Context.Bot.Stop();
            Context.Bot.Dispose();
            Environment.Exit(0);
        }

        [Command("exit", RunMode = RunMode.Async)]
        public async Task ExitAsync() {
            await Context.Discord.StopAsync();
            Environment.Exit(0);
        }

        [Command(help)]
        public async Task HelpAsync(string command = "") {
            if (!checkRun()) return;

            if (command != "") {
                bool success = false;
                foreach (var method in typeof(Commands).GetMethods()) {
                    CommandAttribute cmd = (CommandAttribute)method.GetCustomAttributes(typeof(CommandAttribute), false).FirstOrDefault();
                    if (cmd != null && cmd.Text == command) {
                        success = true;
                        string help = Context.Agent.Help(cmd, method.GetParameters().Length);
                        await ReplyAsync("", false, new EmbedBuilder()
                            .WithTitle("Command: " + cmd.Text + " " + string.Join(" ", method.GetParameters().Select(p => "<" + p.Name + ">")))
                            .WithDescription(help ?? "No help found.")
                            .WithColor(Utils.color_info)
                        );
                        Context.Feedback = CommandFeedback.Handled;
                    }
                }
                if (!success) {
                    Context.Feedback = CommandFeedback.Warning;
                    await ReplyAsync(say(BotString.warning_unknownCommand, command));
                    return;
                }
            } else {
                await ReplyAsync("", false, new EmbedBuilder()
                    .WithTitle(AudioStreamBot.Title)
                    .WithUrl(AudioStreamBot.Url)
                    .WithDescription(say(BotString.info_help))
                    .WithColor(Utils.color_info)
                );
                Context.Feedback = CommandFeedback.Handled;
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

            await ReplyAsync("", false, new EmbedBuilder()
               .WithTitle("Available commands")
               .WithDescription(string.Join("・", commands.Select(c => c.Text).OrderBy(c => c).Distinct().Select(c => "`" + c + "`")))
               .WithColor(Utils.color_info)
           );
            Context.Feedback = CommandFeedback.Handled;
        }

        [Command(assignPermissions)]
        public async Task AssignPermissionsAsync(string user_or_role, [Remainder] string commands) {
            if (!checkRun()) return;

            if (isGuildContext()) {
                // assign role permissions
                var role = Utils.ParseRole(Context.Guild, user_or_role);
                if (role != null) {
                    PermissionDictionary<ulong> permissions;
                    if (Context.Config.commandsRolePermissions.ContainsKey(Context.Guild.Id)) {
                        permissions = Context.Config.commandsRolePermissions[Context.Guild.Id];
                    } else {
                        permissions = new PermissionDictionary<ulong>();
                        Context.Config.commandsRolePermissions.Add(Context.Guild.Id, permissions);
                    }

                    foreach (var command in commands.Split(' ')) {
                        permissions.Assign(role.Id, command);
                    }
                    Context.SaveConfig();
                    return;
                }
            }

            // assign user permissions
            var user = Utils.ParseUser(Context.Discord, user_or_role);
            if (user == null) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_badUser));
                return;
            }

            foreach (var command in commands.Split(' ')) {
                Context.Config.commandsUserPermissions.Assign(user.Id, command);
            }
            Context.SaveConfig();
        }

        [Command(listPermissions)]
        public async Task ListPermissionsAsync(string user_or_role = "") {
            if (!checkRun()) return;

            bool user__role = true;
            SocketUser user = null;
            SocketRole role = null;
            if (user_or_role == "") {
                user = Context.Message.Author;
                role = null;
                user__role = true;
            } else {
                if (isGuildContext()) {
                    role = Utils.ParseRole(Context.Guild, user_or_role);
                    if (role != null) {
                        user__role = false;
                    }
                }
                if (user__role == true) {
                    user = Utils.ParseUser(Context.Discord, user_or_role);
                }
            }
            if (user__role == true && user == null) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_badUser));
                return;
            }

            StringBuilder permissionsString = new StringBuilder();
            var permissions = new HashSet<string>();
            foreach (var method in GetType().GetMethods()) {
                CommandAttribute cmd = (CommandAttribute)method.GetCustomAttributes(typeof(CommandAttribute), false).FirstOrDefault();
                if (cmd != null) {
                    bool runnable;
                    if (user__role) {
                        runnable = canUserRun(user.Id, cmd.Text) == Permission.Accept;
                    } else {
                        runnable = canRoleRun(role.Id, cmd.Text) == Permission.Accept;
                    }
                    if (runnable) {
                        permissions.Add(cmd.Text);
                    }
                }
            }

            permissionsString.Append(string.Join("・", permissions.OrderBy(c => c).Select(c => "`" + c + "`")));
            permissionsString.AppendLine(".");
            await ReplyAsync("", false, new EmbedBuilder()
                .WithTitle(user_or_role == "" ? say(BotString.info_permissionsSelf) : (say(BotString.info_permissionsOther, user__role ? user.Username : "@" + role.Name)))
                .WithDescription(permissionsString.ToString())
                .WithColor(Utils.color_info)
            );
            Context.Feedback = CommandFeedback.Handled;
        }
        #endregion

        #region other
        [Command(showApplicationsLink)]
        public async Task ShowApplicationsLinkAsyc() {
            if (!checkRun()) return;

            await ReplyAsync("", false, new EmbedBuilder()
              .WithTitle(say(BotString.info_applications))
              .WithDescription(string.Format(Utils.link_applications, Context.Discord.CurrentUser.Id))
              .WithColor(Utils.color_info)
            );
            Context.Feedback = CommandFeedback.Handled;
        }

        [Command(showAuthorizeLink)]
        public async Task ShowInviteLinkAsync() {
            if (!checkRun()) return;

            await ReplyAsync("", false, new EmbedBuilder()
              .WithTitle(say(BotString.info_authorize))
              .WithDescription(string.Format(Utils.link_authorize, Context.Discord.CurrentUser.Id))
              .WithColor(Utils.color_info)
            );
            Context.Feedback = CommandFeedback.Handled;
        }

        [Command(sendMail)]
        public async Task SendMailAsync(string recipient, [Remainder] string message) {
            if (!checkRun()) return;

            var users = Utils.ParseUsers(Context.Discord, recipient, Context.Guild);
            if (users == null) {
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.warning_badUser));
                return;
            }
            foreach (var user in users) {
                var channel = await user.GetOrCreateDMChannelAsync();

                await channel.SendMessageAsync(say(BotString.info_newMail), false, new EmbedBuilder()
                  .WithAuthor(Context.Message.Author)
                  .WithDescription(message)
                  .WithColor(Utils.color_info)
                );
            }
        }

        [Command(ping)]
        public async Task PingAsync([Remainder] string host = "") {
            if (!checkRun()) return;

            if (host == "") {
                await ReplyAsync(say(BotString.info_pingSelf, Context.Discord.Latency));
            } else {
                try {
                    var ping = new Ping();
                    var pong = await ping.SendPingAsync(host);
                    if (pong.Status == IPStatus.Success) {
                        await ReplyAsync(say(BotString.info_pingSuccess, pong.RoundtripTime));
                        return;
                    }
                } catch (PingException) {
                }
                Context.Feedback = CommandFeedback.Warning;
                await ReplyAsync(say(BotString.info_pingFailed));
            }
        }
        #endregion

        private bool checkRun(int stackOffset = 0) {
            const int stackIndex = 3;
            var stackTrace = new StackTrace();
            var commandMethod = stackTrace.GetFrame(stackIndex + stackOffset).GetMethod();
            CommandAttribute cmd = (CommandAttribute)commandMethod.GetCustomAttributes(typeof(CommandAttribute), false).Single();
            return checkRun(cmd.Text);
        }

        private bool checkRun(string command) {
            bool result = canUserRun(Context.Message.Author.Id, command) == Permission.Accept;
            if (!result) {
                Context.Feedback = CommandFeedback.NoEntry;
            }
            return result;
        }

        private Permission canUserRun(ulong userId, string command) {
            if (Context.Config.ownerId.HasValue && userId == Context.Config.ownerId) {
                return Permission.Accept;
            }
            if (isGuildContext()) {
                Permission rolePermission = canRoleRun(userId, command, true);
                if (rolePermission != Permission.Default) {
                    return rolePermission;
                }
            }
            return Context.Config.commandsUserPermissions.Check(userId, command, Context.Config.commandsDefaultPermissions);
        }

        private Permission canRoleRun(ulong entityId, string command, bool userId__roleId = false) {
            PermissionDictionary<ulong> rolePermissions;
            if (Context.Config.commandsRolePermissions.TryGetValue(Context.Guild.Id, out rolePermissions)) {
                IEnumerable<SocketRole> roles;
                if (userId__roleId) {
                    var user = Context.Guild.GetUser(entityId);
                    roles = user.Roles.OrderByDescending(r => r.Position);
                } else {
                    var role_ = Context.Guild.GetRole(entityId);
                    roles = Context.Guild.Roles.OrderByDescending(r => r.Position).Where(r => r.Position <= role_.Position);
                }
                foreach (var role in roles) {
                    Permission rolePermission = rolePermissions.Check(role.Id, command, Context.Config.commandsDefaultPermissions);
                    if (rolePermission != Permission.Default) {
                        return rolePermission;
                    }
                }
            }
            return Permission.Default;
        }

        private bool isGuildContext() {
            return Context.Guild != null;
        }

        private bool checkGuildContext() {
            bool result = isGuildContext();
            if (!result) {
                Context.Feedback = CommandFeedback.GuildContextRequired;
            }
            return result;
        }

        private string say(BotString @string) {
            return Context.Agent.Say(@string);
        }

        private string say(BotString @string, params object[] args) {
            return Context.Agent.Say(@string, args);
        }
    }
}
