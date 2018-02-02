using Discord;
using Discord.Commands;
using System.Net.NetworkInformation;
using System.Threading.Tasks;

namespace DASB {
    [Group("utils")]
    public class UtilsModule : CommandsModule {
        [Command("mail"), Alias("whisper")]
        [Summary("Sends a direct message *to* a user, users with role, or users in a channel.")]
        public async Task SendMail(string to, [Remainder] string message) {
            var users = Utils.ParseUsers(Context.Discord, to, Context.Guild);
            if (users == null) {
                await ReplyAsync(Say(BotString.warning_badUser));
                return;
            }
            foreach (var user in users) {
                await user.SendMessageAsync(Say(BotString.info_newMail), false, new EmbedBuilder()
                  .WithAuthor(Context.Message.Author)
                  .WithDescription(message)
                  .WithColor(Utils.color_info)
                );
            }
        }

        [Command("ping")]
        [Summary("Checks if a *host* is available.")]
        [DefaultPermission(Permission.Accept)]
        public async Task Ping([Remainder] string host = "") {
            if (host == "") {
                await ReplyAsync(Say(BotString.info_pingSelf, Context.Discord.Latency));
            } else {
                try {
                    var ping = new Ping();
                    var pong = ping.Send(host);
                    if (pong.Status == IPStatus.Success) {
                        await ReplyAsync(Say(BotString.info_pingSuccess, pong.RoundtripTime));
                        return;
                    }
                } catch (PingException) {
                }
                await ReplyAsync(Say(BotString.info_pingFailed));
            }
        }
    }
}
