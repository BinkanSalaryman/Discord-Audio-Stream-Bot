using DASB;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections.Generic;

namespace DiscordAudioStreamBot {
    [TestClass]
    public class PermissionDictionaryTest {
        private HashSet<string> @default = new HashSet<string>();
        private PermissionDictionary<int> data = null;
        private void reset() {
            data = new PermissionDictionary<int>();
            data.Add(0, "member");
            data.Add(1, "*");
            data.Add(2, "!*");
        }

        private Permission check(int index, string member) {
            return data.Check(index, member, @default);
        }

        [TestMethod]
        public void check_defaultNone() {
            reset();
            Assert.AreEqual(Permission.Default, check(-1, "member"));
            Assert.AreEqual(Permission.Accept, check(0, "member"));
            Assert.AreEqual(Permission.Accept, check(1, "member"));
            Assert.AreEqual(Permission.Reject, check(2, "member"));

            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(Permission.Accept, check(-1, "member"));

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(Permission.Accept, check(0, "member"));

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(Permission.Accept, check(1, "member"));

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(Permission.Accept, check(2, "member"));
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(Permission.Reject, check(-1, "member"));

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(Permission.Reject, check(0, "member"));

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(Permission.Reject, check(1, "member"));

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(Permission.Reject, check(2, "member"));
        }

        [TestMethod]
        public void check_defaultMember() {
            @default.Add("member");

            reset();
            Assert.AreEqual(Permission.Accept, check(-1, "member"));
            Assert.AreEqual(Permission.Accept, check(0, "member"));
            Assert.AreEqual(Permission.Accept, check(1, "member"));
            Assert.AreEqual(Permission.Accept, check(2, "member"));
            //--
            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(Permission.Accept, check(-1, "member"));

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(Permission.Accept, check(0, "member"));

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(Permission.Accept, check(1, "member"));

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(Permission.Accept, check(2, "member"));
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(Permission.Reject, check(-1, "member"));

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(Permission.Reject, check(0, "member"));

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(Permission.Reject, check(1, "member"));

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(Permission.Reject, check(2, "member"));
        }

        [TestMethod]
        public void check_defaultBlacklist() {
            @default.Add("*");

            reset();
            Assert.AreEqual(Permission.Accept, check(-1, "member"));
            Assert.AreEqual(Permission.Accept, check(0, "member"));
            Assert.AreEqual(Permission.Accept, check(1, "member"));
            Assert.AreEqual(Permission.Reject, check(2, "member"));
            //--
            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(Permission.Accept, check(-1, "member"));

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(Permission.Accept, check(0, "member"));

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(Permission.Accept, check(1, "member"));

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(Permission.Accept, check(2, "member"));
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(Permission.Reject, check(-1, "member"));

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(Permission.Reject, check(0, "member"));

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(Permission.Reject, check(1, "member"));

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(Permission.Reject, check(2, "member"));
        }

        [TestMethod]
        public void check_defaultWhitelist() {
            @default.Add("!*");

            reset();
            Assert.AreEqual(Permission.Reject, check(-1, "member"));
            Assert.AreEqual(Permission.Accept, check(0, "member"));
            Assert.AreEqual(Permission.Accept, check(1, "member"));
            Assert.AreEqual(Permission.Reject, check(2, "member"));
            //--
            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(Permission.Accept, check(-1, "member"));

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(Permission.Accept, check(0, "member"));

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(Permission.Accept, check(1, "member"));

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(Permission.Accept, check(2, "member"));
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(Permission.Reject, check(-1, "member"));

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(Permission.Reject, check(0, "member"));

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(Permission.Reject, check(1, "member"));

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(Permission.Reject, check(2, "member"));
        }
    }
}
