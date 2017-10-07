using Discord.Commands;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;

namespace DASB.Test {
    [TestClass]
    public class BotAgentTest {
        private static IEnumerable<IBotAgent> agents = AudioStreamBot.Services.GetServices<IBotAgent>();

        [TestMethod]
        public void check_hasAgents() {
            if (!agents.GetEnumerator().MoveNext()) {
                Assert.Fail("No bot agents found!");
            }
        }

        [TestMethod]
        public void check_canSayAll() {
            foreach (var agent in agents) {
                foreach (var @string in (BotString[])Enum.GetValues(typeof(BotString))) {
                    Assert.IsNotNull(agent.Say(@string));
                }
            }
        }

        [TestMethod]
        public void check_canHelpAll() {
            foreach(var agent in agents) {
                foreach (var method in typeof(Commands).GetMethods()) {
                    CommandAttribute cmd = (CommandAttribute)method.GetCustomAttributes(typeof(CommandAttribute), false).FirstOrDefault();
                    if (cmd != null) {
                        Assert.IsNotNull(agent.Help(cmd, method.GetParameters().Length));
                    }
                }
            }
        }
    }
}
