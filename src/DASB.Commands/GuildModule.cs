using Discord;
using Discord.Commands;
using System.Linq;
using System.Threading.Tasks;

namespace DASB {
    [Group("guild")]
    public class GuildModule : CommandsModule {
        [Command("join")]
        [Summary("Shows authorization link for adding this bot to a guild.")]
        [DisablePermissionCheck]
        public async Task JoinGuild() {
            await ReplyAsync("", false, new EmbedBuilder()
              .WithTitle(Say(BotString.info_authorize))
              .WithDescription(string.Format(Utils.link_discordAuthorize, Context.Discord.CurrentUser.Id))
              .WithColor(Utils.color_info)
            );
        }

        [Command("leave")]
        [Summary("Leaves the current guild.")]
        [RequireGuildContext]
        [DefaultPermission(Permission.Reject)]
        public async Task LeaveGuild() {
            await ReplyAsync(Say(BotString.info_goodbyeGuild));
            await Context.Guild.LeaveAsync();
        }

        [Command("context"), Alias("")]
        [Summary("Runs *command* within context of *guild*.")]
        [DisablePermissionCheck]
        public async Task WithGuildContext(string guild, [Remainder] string command) {
            var guild_ = Utils.ParseGuild(Context.Discord, guild).ToList();
            switch (guild_.Count) {
                case 0:
                    await ReplyAsync(Say(BotString.warning_badGuild));
                    return;
                case 1:
                    await Context.Bot.RunCommand(Context.Message, Context.Message.Content.LastIndexOf(command), guild_.Single());
                    break;
                default:
                    await ReplyAsync(Say(BotString.warning_guildNameAmbigous));
                    return;
            }
        }
    }
}
