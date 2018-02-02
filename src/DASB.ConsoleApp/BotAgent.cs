using Discord;
using Discord.Commands;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    public class BotAgent {
        public string Id { get; set; }
        
        public Dictionary<string, string> SayDictionary = new Dictionary<string, string>();

        public BotAgent() {

        }

        public BotAgent(string json) {
            Load(JToken.Parse(json));
        }

        public override string ToString() {
            return ToJson().ToString(Formatting.Indented);
        }

        public void Load(JToken root) {
            JTokenStack stack = new JTokenStack(root);

            SayDictionary = readDictionary((JObject)stack.Get("say"));

            stack.Pop();
        }

        public JToken ToJson() {
            JTokenStack stack = new JTokenStack(new JObject());

            stack.Set("say", writeDictionary(SayDictionary));

            return stack.Pop();
        }

        private static Dictionary<string, string> readDictionary(JObject node) {
            var result = new Dictionary<string, string>();
            foreach (var entry in node) {
                result.Add(entry.Key, (string)entry.Value);
            }
            return result;
        }

        private static JObject writeDictionary(Dictionary<string, string> entries) {
            var result = new JObject();
            foreach (var entry in entries) {
                result.Add(entry.Key, entry.Value);
            }
            return result;
        }

        public string Say(string key) {
            string value;
            if (SayDictionary.TryGetValue(key, out value)) {
                return value;
            } else {
                Utils.Log(LogSeverity.Warning, GetType(), "Missing translation for saying \"" + key + "'\"");
                return key;
            }
        }

        public string Say(string @string, params object[] args) {
            return string.Format(Say(@string), args);
        }

        public string Say(BotString @string) {
            return Say(@string.ToString());
        }

        public string Say(BotString @string, params object[] args) {
            return string.Format(Say(@string), args);
        }
    }
}
