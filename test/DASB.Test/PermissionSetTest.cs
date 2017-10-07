using DASB;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DiscordAudioStreamBot {
    [TestClass]
    public class PermissionDictionaryTest {
        private Predicate<string> @default = null;
        private PermissionDictionary<int> data = null;
        private void reset () {
            data = new PermissionDictionary<int>();
            data.Add(0, "member");
            data.Add(1, "*");
            data.Add(2, "!*");
        }

        private bool check(int index, string member) {
            return data.Check(index, member, @default);
        }

        [TestMethod]
        public void check_defaultNone() {
            @default = x => false;

            reset();
            Assert.AreEqual(data.Check(-1, "member"), false);
            Assert.AreEqual(data.Check(0, "member"), true);
            Assert.AreEqual(data.Check(1, "member"), true);
            Assert.AreEqual(data.Check(2, "member"), false);

            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(data.Check(-1, "member"), true);

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(data.Check(0, "member"), true);

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(data.Check(1, "member"), true);

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(data.Check(2, "member"), true);
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(data.Check(-1, "member"), false);

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(data.Check(0, "member"), false);

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(data.Check(1, "member"), false);

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(data.Check(2, "member"), false);
        }

        [TestMethod]
        public void check_defaultMember() {
            @default = x => x == "member";

            reset();
            Assert.AreEqual(check(-1, "member"), true);
            Assert.AreEqual(check(0, "member"), true);
            Assert.AreEqual(check(1, "member"), true);
            Assert.AreEqual(check(2, "member"), true);
            //--
            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(check(-1, "member"), true);

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(check(0, "member"), true);

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(check(1, "member"), true);

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(check(2, "member"), true);
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(check(-1, "member"), false);

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(check(0, "member"), false);

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(check(1, "member"), false);

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(check(2, "member"), false);
        }

        [TestMethod]
        public void check_defaultBlacklist() {
            @default = x => x == "*";

            reset();
            Assert.AreEqual(check(-1, "member"), true);
            Assert.AreEqual(check(0, "member"), true);
            Assert.AreEqual(check(1, "member"), true);
            Assert.AreEqual(check(2, "member"), false);
            //--
            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(check(-1, "member"), true);

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(check(0, "member"), true);

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(check(1, "member"), true);

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(check(2, "member"), true);
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(check(-1, "member"), false);

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(check(0, "member"), false);

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(check(1, "member"), false);

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(check(2, "member"), false);
        }

        [TestMethod]
        public void check_defaultWhitelist() {
            @default = x => x == "!*";

            reset();
            Assert.AreEqual(check(-1, "member"), false);
            Assert.AreEqual(check(0, "member"), true);
            Assert.AreEqual(check(1, "member"), true);
            Assert.AreEqual(check(2, "member"), false);
            //--
            reset();
            data.Grant(-1, "member");
            Assert.AreEqual(check(-1, "member"), true);

            reset();
            data.Grant(0, "member");
            Assert.AreEqual(check(0, "member"), true);

            reset();
            data.Grant(1, "member");
            Assert.AreEqual(check(1, "member"), true);

            reset();
            data.Grant(2, "member");
            Assert.AreEqual(check(2, "member"), true);
            //--
            reset();
            data.Revoke(-1, "member");
            Assert.AreEqual(check(-1, "member"), false);

            reset();
            data.Revoke(0, "member");
            Assert.AreEqual(check(0, "member"), false);

            reset();
            data.Revoke(1, "member");
            Assert.AreEqual(check(1, "member"), false);

            reset();
            data.Revoke(2, "member");
            Assert.AreEqual(check(2, "member"), false);
        }
    }
}
