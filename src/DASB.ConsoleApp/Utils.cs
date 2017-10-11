using Discord;
using Discord.WebSocket;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace DASB {
    internal static class Utils {
        public static Emoji emoji_success => new Emoji(char.ConvertFromUtf32(0x2705));
        public static Emoji emoji_forbidden => new Emoji(char.ConvertFromUtf32(0x26d4));
        public static Emoji emoji_warning => new Emoji(char.ConvertFromUtf32(0x26a0));
        public static Emoji emoji_error => new Emoji(char.ConvertFromUtf32(0x0001f916));
        public static Emoji emoji_exit => new Emoji(char.ConvertFromUtf32(0x0001f44b));

        public static Color color_info => new Color(0xf0ff40);

        public static readonly string link_applications = "https://discordapp.com/developers/applications/me";
        public static readonly string link_authorize = "https://discordapp.com/oauth2/authorize?client_id={0}&scope=bot&permissions=0";

        public static SocketUser ParseUser(DiscordSocketClient discord, string text) {
            ulong userId;
            if (MentionUtils.TryParseUser(text, out userId)) {
                return discord.GetUser(userId);
            }
            int discriminatorIdx = text.IndexOf('#');
            if (text.StartsWith("@") && discriminatorIdx >= 0) {
                string username = text.Substring(1, discriminatorIdx - 1);
                string discriminator = text.Substring(discriminatorIdx + 1);
                return discord.GetUser(username, discriminator);
            }
            if (ulong.TryParse(text, out userId)) {
                return discord.GetUser(userId);
            }
            return null;
        }

        public static IEnumerable<SocketUser> ParseUsers(DiscordSocketClient discord, string text, SocketGuild guild) {
            if (guild != null) {
                ulong roleId;
                if (MentionUtils.TryParseRole(text, out roleId)) {
                    return guild.Users.Where(u => u.Roles.Any(r => r.Id == roleId));
                }
            }
            ulong channelId;
            if (MentionUtils.TryParseChannel(text, out channelId)) {
                return discord.GetChannel(channelId).Users;
            }
            var user = ParseUser(discord, text);
            return user != null ? new[] { user } : null;
        }

        public static SocketRole ParseRole(SocketGuild guild, string text) {
            ulong roleId;
            if(MentionUtils.TryParseRole(text, out roleId)) {
                return guild.GetRole(roleId);
            }
            return null;
        }

        public static UserStatus? ParseUserStatus(string status) {
            switch (status.ToLowerInvariant()) {
                case "online":
                    return UserStatus.Online;
                case "idle":
                    return UserStatus.Idle;
                case "afk":
                    return UserStatus.AFK;
                case "dnd":
                    return UserStatus.DoNotDisturb;
                case "invisible":
                    return UserStatus.Invisible;
                case "offline":
                    return UserStatus.Offline;
                default:
                    return null;
            }
        }

        public static void Log(LogSeverity severity, Type source, string message, Exception ex = null) {
            Log(severity, source.Name, message, ex);
        }

        public static void Log(LogSeverity severity, string source, string message, Exception ex = null) {
            Log(new LogMessage(severity, source, message, ex));
        }

        public static void Log(LogMessage msg) {
            switch (msg.Severity) {
#if DEBUG
                case LogSeverity.Debug:
#endif
                case LogSeverity.Verbose:
                case LogSeverity.Info:
                case LogSeverity.Warning:
                case LogSeverity.Error:
                case LogSeverity.Critical:
                    Console.ForegroundColor = getLogForeground(msg.Severity);
                    Console.WriteLine(msg);
                    break;
            }
        }

        private static ConsoleColor getLogForeground(LogSeverity severity) {
            switch (severity) {
                case LogSeverity.Debug:
                    return ConsoleColor.DarkGray;
                case LogSeverity.Verbose:
                    return ConsoleColor.Gray;
                case LogSeverity.Info:
                    return ConsoleColor.White;
                case LogSeverity.Warning:
                    return ConsoleColor.Yellow;
                case LogSeverity.Error:
                case LogSeverity.Critical:
                    return ConsoleColor.Red;
                default:
                    return ConsoleColor.Magenta;
            }
        }
    }
}
