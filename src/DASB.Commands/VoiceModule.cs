using Discord.Commands;
using Discord.WebSocket;
using System.Linq;
using System.Threading.Tasks;

namespace DASB {
    [Group("voice")]
    public class VoiceModule : CommandsModule {
        [Command("join")]
        [Summary("Joins a voice *channel*.")]
        [RequireGuildContext]
        public async Task JoinVoice([Remainder] string channel = null) {
            var self = Context.Guild.GetUser(Context.Discord.CurrentUser.Id);

            if (channel != null) {
                // find voice channel
                SocketVoiceChannel channel_;
                ulong channelId;
                if (ulong.TryParse(channel, out channelId)) {
                    channel_ = Context.Guild.GetVoiceChannel(channelId);
                    if (channel_ == null) {
                        await ReplyAsync(Say(BotString.warning_badVoiceChannelId));
                        return;
                    }
                } else {
                    var channels = Context.Guild.VoiceChannels.Where(v => v.Name == channel).ToList();
                    if (channels.Count == 0) {
                        await ReplyAsync(Say(BotString.warning_voiceChannelNameNotFound));
                        return;
                    }
                    if (channels.Count > 1) {
                        await ReplyAsync(Say(BotString.warning_voiceChannelNameAmbigous));
                        return;
                    }
                    channel_ = channels[0];
                }
                if (!self.GetPermissions(channel_).Connect) {
                    await ReplyAsync(Say(BotString.warning_noPermissionConnect_specifiedChannel));
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
                    await ReplyAsync(Say(BotString.warning_notInGuildAuthor));
                }

                if (channel_ != null) {
                    if (!self.GetPermissions(channel_).Connect) {
                        // missing permissions
                        await ReplyAsync(Say(BotString.warning_noPermissionConnect_authorsChannel));
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

        [Command("leave")]
        [Summary("Leaves the current guilds' voice channel.")]
        [RequireGuildContext]
        public async Task LeaveVoice() {
            if (!Context.Bot.IsInVoice(Context.Guild.Id)) {
                await ReplyAsync(Say(BotString.warning_notInVoice));
                return;
            }
            await Context.Bot.LeaveVoice(Context.Guild.Id);
        }

        [Command("leave-all")]
        [Summary("Leaves from all connected voice channels.")]
        public async Task LeaveVoiceAll() {
            await Context.Bot.LeaveVoiceAll();
        }
    }
}
