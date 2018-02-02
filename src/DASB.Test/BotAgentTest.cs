using Discord.Commands;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;

namespace DASB.Test {
    [TestClass]
    public class BotAgentTest {
        private BotAgent agent = new BotAgent(File.ReadAllText(@"agents\tomoko.json"));

        [TestMethod]
        public void check_canSayAll() {
            var missing = new List<BotString>();
            foreach (var @string in (BotString[])Enum.GetValues(typeof(BotString))) {
                if(agent.Say(@string) == null) {
                    missing.Add(@string);
                }
            }
            if(missing.Count > 0) {
                Assert.Fail("Can't say:\n" + string.Join("\n", missing.Select(s => s.ToString())));
            }
        }

        [TestMethod]
        public void check_canHelpAll() {
            var missing = new List<MethodInfo>();
            foreach (var method in typeof(CommandsModule).GetMethods()) {
                CommandAttribute cmd = (CommandAttribute)method.GetCustomAttributes(typeof(CommandAttribute), false).FirstOrDefault();
                if (cmd != null && agent.Help(method, cmd) == null) {
                    missing.Add(method);
                }
            }
            if(missing.Count > 0) {
                Assert.Fail("Can't help:\n" + string.Join("\n", missing.Select(m => m.ToString())));
            }
        }
    }
}
