using Newtonsoft.Json.Linq;
using System.Collections.Generic;
using System.IO;

namespace DASB {
    public class JTokenStack : Stack<JToken> {
        public JTokenStack() {
            
        }

        public JTokenStack(JToken root) {
            Push(root);
        }

        public void Push(string childName) {
            var token = Peek();
            var child = token[childName];
            Push(child);
        }

        public void PushNew(string childName) {
            var token = Peek();
            var child = new JObject();
            token[childName] = child;
            Push(child);
        }

        public JToken Get(string childName) {
            var token = Peek();
            var child = token[childName];
            if (child == null) {
                throw new IOException("\"" + childName + "\" not found in " + token.Path);
            }
            return child;
        }

        public void Set(string childName, JToken value) {
            var token = Peek();
            token[childName] = value;
        }
    }
}
