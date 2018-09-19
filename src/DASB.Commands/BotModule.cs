using Discord;
using Discord.Commands;
using System;
using System.Threading.Tasks;

namespace DASB {
    [Group("bot"), Alias("")]
    public class BotModule : CommandsModule {
        [Command("streaming"), Alias("stream")]
        [Summary("Sets this bot's game status to **Playing** *game*.")]
        public async Task SetStreaming([Remainder] string game = "") {
            await Context.Discord.SetGameAsync(game, Utils.link_twitchDummyStream, StreamType.Twitch);
        }

        [Command("playing"), Alias("play")]
        [Summary("Sets this bot's game status to **Streaming** *game*.")]
        public async Task SetPlaying([Remainder] string game = "") {
            await Context.Discord.SetGameAsync(game);
        }

        [Command("listening"), Alias("listen")]
        [Summary("Sets this bot's game status to **Listening** *game*.")]
        public async Task SetListening([Remainder] string game = "") {
            await Context.Discord.SetGameAsync(game, null, (StreamType)2);
        }

        [Command("status")]
        [Summary("Sets this bot's online status to *status*. Allowed values: online, idle, afk, dnd, invisible, offline.")]
        public async Task SetStatus(string status) {
            var status_ = Utils.ParseUserStatus(status ?? "online");
            if (!status_.HasValue) {
                await ReplyAsync(Say(BotString.warning_badStatus));
                return;
            }
            await Context.Discord.SetStatusAsync(status_.Value);
        }

        [Command("nickname"), Alias("nick")]
        [Summary("Sets this bot's nickname to *nickname*.")]
        [RequireGuildContext]
        public async Task SetNickname([Remainder] string nickname = "") {
            await Context.Guild.GetUser(Context.Discord.CurrentUser.Id).ModifyAsync(x => x.Nickname = nickname);
        }

        [Command("stop")]
        [Summary("Stops this bot's process.")]
        [DefaultPermission(Permission.Reject)]
        public async Task Stop() {
            await ReplyAsync(Say(BotString.info_goodbye));
            await Context.Bot.Stop();
            Context.Bot.Dispose();
            Environment.Exit(0);
        }
    }
}
