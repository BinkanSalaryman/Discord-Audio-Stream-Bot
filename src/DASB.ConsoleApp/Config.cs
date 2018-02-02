using Discord;
using Discord.Audio;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;

namespace DASB {
    public class Config {
        #region config properties
        /// <summary>
        /// bot's token to identify and log into bot account, required
        /// </summary>
        public string botToken = null;

        /// <summary>
        /// flag wether the bot should speak in voice chat
        /// </summary>
        public bool speakEnabled = true;
        /// <summary>
        /// audio recording device name to pipe data into discord voice
        /// special values: null for default recording device
        /// </summary>
        public string speakRecordingDevice = null;
        /// <summary>
        /// network audio stream encoding hint for the bot when speaking, default is mixed voice/music
        /// can be any of: "Voice", "Music", "Mixed"
        /// </summary>
        public AudioApplication speakAudioType = AudioApplication.Mixed;
        /// <summary>
        /// network audio stream bit rate for the bot when speaking, default is 96 KiBit
        /// special values: null to use channel's bit rate
        /// </summary>
        public int? speakBitRate = 96 * 1024;
        /// <summary>
        /// network audio stream buffer size in milliseconds for the bot when speaking, default is 1000 ms
        /// </summary>
        public int speakBufferMillis = 1000;

        /// <summary>
        /// flag wether the bot should listen in voice chat
        /// </summary>
        public bool listenEnabled = false;
        /// <summary>
        /// audio playback device name to pipe data from discord voice
        /// special values: null for default playback device
        /// </summary>
        public string listenPlaybackDevice = null;
        
        /// <summary>
        /// bot's language implementation, cannot be null
        /// </summary>
        public string commandsBotAgent = "tomoko";
        /// <summary>
        /// default command permissions, may be overridden by per-user permissions
        /// </summary>
        public HashSet<string> commandsDefaultPermissions = new HashSet<string>();
        /// <summary>
        /// command permissions for users
        /// </summary>
        public PermissionDictionary<ulong> commandsUserPermissions = new PermissionDictionary<ulong>();
        /// <summary>
        /// command permissions for roles, per guild 
        /// </summary>
        public Dictionary<ulong, PermissionDictionary<ulong>> commandsRolePermissions = new Dictionary<ulong, PermissionDictionary<ulong>>();
        #endregion
        
        public Config() {

        }

        public Config(string json) {
            Load(JToken.Parse(json));
        }

        public override string ToString() {
            return ToJson().ToString(Formatting.Indented);
        }

        #region I/O
        public void Load(JToken root) {
            var stack = new JTokenStack(root);

            // #general
            stack.Push("general");
            botToken = (string)stack.Get("botToken");
            stack.Pop();
            // #

            // #voice
            stack.Push("voice");

            // #voice - speak
            stack.Push("speak");
            speakEnabled = (bool)stack.Get("enabled");
            speakRecordingDevice = (string)stack.Get("recordingDevice");
            speakAudioType = (AudioApplication)Enum.Parse(typeof(AudioApplication), (string)stack.Get("audioType"));
            speakBitRate = (int?)stack.Get("bitRate");
            speakBufferMillis = (int)stack.Get("bufferMillis");
            stack.Pop();
            // #voice

            // #voice - listen
            stack.Push("listen");
            listenEnabled = (bool)stack.Get("enabled");
            listenPlaybackDevice = (string)stack.Get("playbackDevice");
            stack.Pop();
            // #voice

            stack.Pop();
            // #

            // #text
            stack.Push("text");

            // #text - commands
            stack.Push("commands");
            commandsBotAgent = (string)stack.Get("botAgent");
            if(commandsBotAgent == null) {
                throw new FormatException("bot agent cannot be null");
            }

            // #text - commands - permissions
            stack.Push("permissions");
            commandsDefaultPermissions.Clear();
            foreach (var defaultPermission in stack.Get("defaultPermissions").Value<string>().Split(' ')) {
                commandsDefaultPermissions.Add(defaultPermission);
            }
            commandsUserPermissions = readUserPermissions((JObject)stack.Get("userPermissions"));
            commandsRolePermissions = readRolePermissions((JObject)stack.Get("rolePermissions"));
            stack.Pop();
            // #text - commands

            stack.Pop();
            // #text

            stack.Pop();
            // #

            stack.Pop();
        }

        public JToken ToJson() {
            var stack = new JTokenStack(new JObject());

            // #general
            stack.PushNew("general");
            stack.Set("botToken", botToken);

            stack.Pop();
            // #

            // #voice
            stack.PushNew("voice");

            // #voice - speak
            stack.PushNew("speak");
            stack.Set("enabled", speakEnabled);
            stack.Set("recordingDevice", speakRecordingDevice);
            stack.Set("audioType", Enum.GetName(typeof(AudioApplication), speakAudioType));
            stack.Set("bitRate", speakBitRate);
            stack.Set("bufferMillis", speakBufferMillis);
            stack.Pop();
            // #voice

            // #voice - listen
            stack.PushNew("listen");
            stack.Set("enabled", listenEnabled);
            if (listenEnabled) {
                //Utils.Log(LogSeverity.Warning, typeof(Config), "This feature isn't supported yet.");
                //listenEnabled = false;
            }
            stack.Set("playbackDevice", listenPlaybackDevice);
            stack.Pop();
            // #voice

            stack.Pop();
            // #

            // #text
            stack.PushNew("text");

            // #text - commands
            stack.PushNew("commands");
            stack.Set("botAgent", commandsBotAgent);

            // #text - commands - permissions
            stack.PushNew("permissions");
            stack.Set("defaultPermissions", string.Join(" ", commandsDefaultPermissions));
            stack.Set("userPermissions", writeUserPermissions(commandsUserPermissions));
            stack.Set("rolePermissions", writeRolePermissions(commandsRolePermissions));
            stack.Pop();
            // #text - commands

            stack.Pop();
            // #text

            stack.Pop();
            // #

            return stack.Pop();
        }
        #endregion

        #region complex (de)serialization
        private static JToken writePermissions(PermissionDictionary<ulong> permissions) {
            var entries = new JObject();
            foreach (var p in permissions) {
                entries.Add(p.Key.ToString(), string.Join(" ", p.Value));
            }
            return entries;
        }

        private static PermissionDictionary<ulong> readPermissions(JObject node) {
            var result = new PermissionDictionary<ulong>();
            foreach (var entry in node) {
                ulong key = ulong.Parse(entry.Key);
                var values = new HashSet<string>();
                foreach (var value in entry.Value.Value<string>().Split(' ')) {
                    values.Add(value);
                }
                result.Add(key, values);
            }
            return result;
        }

        private static JToken writeUserPermissions(PermissionDictionary<ulong> userPermissions) {
            return writePermissions(userPermissions);
        }

        private static PermissionDictionary<ulong> readUserPermissions(JObject node) {
            return readPermissions(node);
        }

        private static JToken writeRolePermissions(Dictionary<ulong, PermissionDictionary<ulong>> guildPermissions) {
            var node = new JObject();
            foreach (var rolePermissions in guildPermissions) {
                node.Add(rolePermissions.Key.ToString(), writePermissions(rolePermissions.Value));
            }
            return node;
        }

        private static Dictionary<ulong, PermissionDictionary<ulong>> readRolePermissions(JObject node) {
            var result = new Dictionary<ulong, PermissionDictionary<ulong>>();
            foreach (var entry in node) {
                result.Add(ulong.Parse(entry.Key), readPermissions((JObject)entry.Value));
            }
            return result;
        }
        #endregion
    }
}
