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

        public static UserStatus ParseUserStatus(string status) {
            switch (status) {
                case "online":
                    return UserStatus.Online;
                case "idle":
                    return UserStatus.Idle;
                case "dnd":
                    return UserStatus.DoNotDisturb;
                case "invisible":
                    return UserStatus.Invisible;
                case "offline":
                    return UserStatus.Offline;
                default:
                    throw new ArgumentOutOfRangeException("status");
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

        public static JToken ToJson<T>(PermissionDictionary<T> permissions) {
            var array = new JArray();
            foreach (var p in permissions) {
                array.Add(p.Key);
                array.Add(string.Join(" ", p.Value));
            }
            return array;
        }

        public static PermissionDictionary<T> PermissionDictionaryFromJson<T>(JToken node) {
            var result = new PermissionDictionary<T>();
            var array = (JArray)node;
            const int bs = 2;
            for (int i = 0; i < (array.Count / bs); i++) {
                T key = array[i * bs].Value<T>();
                HashSet<string> values = new HashSet<string>();
                foreach (var value in array[i * bs + 1].Value<string>().Split(' ')) {
                    values.Add(value);
                }
                result.Add(key, values);
            }
            return result;
        }
    }
}
