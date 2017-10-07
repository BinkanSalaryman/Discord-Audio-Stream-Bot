using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    public class PermissionDictionary<TKey> : Dictionary<TKey, HashSet<string>> {
        public const string AllChar = "*";
        public const string NotChar = "!";
        public const string ClearChar = "~";

        public bool Contains(TKey key, string flag) {
            if (ContainsKey(key)) {
                var flags = this[key];
                if (flags != null && flags.Contains(flag)) {
                    return true;
                }
            }
            return false;
        }

        public bool Add(TKey key, string flag) {
            if (ContainsKey(key)) {
                return this[key].Add(flag);
            } else {
                var flags = new HashSet<string>();
                flags.Add(flag);
                Add(key, flags);
                return true;
            }
        }

        public bool Remove(TKey key, string flag) {
            bool changed = ContainsKey(key) && this[key].Remove(flag);
            if (changed && this[key].Count == 0) {
                Remove(key);
            }
            return changed;
        }

        public bool Check(TKey key, string value, Predicate<string> defaultContains = null) {
            if (value.StartsWith(NotChar)) {
                throw new ArgumentException("Cannot check " + NotChar + " permissions");
            }

            bool dblacklist = false;
            bool dwhitelist = false;
            bool daccept = false;
            bool dreject = false;

            if (defaultContains != null) {
                dblacklist = defaultContains(AllChar);
                dwhitelist = defaultContains(NotChar + AllChar);
                daccept = defaultContains(value);
                dreject = defaultContains(NotChar + value);
            }

            bool blacklist = Contains(key, AllChar);
            bool whitelist = Contains(key, NotChar + AllChar);
            bool accept = Contains(key, value);
            bool reject = Contains(key, NotChar + value);

            if (blacklist || (dblacklist && !whitelist)) {
                return accept || !(reject || dreject);
            }
            if (whitelist || (dwhitelist && !blacklist)) {
                return !reject && (accept || daccept);
            }
            return (accept || daccept) && !(reject || dreject);
        }

        public void Grant(TKey key, string value) {
            if (value.StartsWith(NotChar)) {
                throw new ArgumentException("Cannot grant " + NotChar + " permissions");
            }
            if (value == AllChar) {
                if (ContainsKey(key)) {
                    this[key].RemoveWhere(f => f.StartsWith(NotChar));
                }
                Add(key, AllChar);
            } else {
                Remove(key, NotChar + value);
                Add(key, value);
            }
        }

        public void Revoke(TKey key, string value) {
            if (value.StartsWith(NotChar)) {
                throw new ArgumentException("Cannot revoke " + NotChar + " permissions");
            }
            if (value == AllChar) {
                if (ContainsKey(key)) {
                    this[key].RemoveWhere(f => !f.StartsWith(NotChar));
                }
                Add(key, NotChar + AllChar);
            } else {
                Remove(key, value);
                Add(key, NotChar + value);
            }
        }

        public void Assign(TKey key, string value) {
            if (value.StartsWith(ClearChar)) {
                value = value.Substring(1);
                if (value.StartsWith(ClearChar)) {
                    throw new ArgumentException("Cannot clear " + ClearChar + " permissions");
                }
                if (value == AllChar) {
                    Clear();
                } else {
                    Remove(key, value);
                    Remove(key, NotChar + value);
                }
            } else {
                if (value.StartsWith(NotChar)) {
                    Revoke(key, value.Substring(1));
                } else {
                    Grant(key, value);
                }
            }
        }
    }
}
