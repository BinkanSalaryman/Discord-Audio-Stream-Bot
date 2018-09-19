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

        public PermissionMode GetMode(TKey key) {
            return GetMode(key, null);
        }

        public PermissionMode GetMode(TKey key, Func<PermissionMode> parent_Mode) {
            var pmode = PermissionMode.Default;
            if (parent_Mode != null) {
                pmode = parent_Mode();
            }

            bool blacklist = Contains(key, AllChar);
            bool whitelist = Contains(key, NotChar + AllChar);
            if (blacklist || (pmode == PermissionMode.Blacklist && !whitelist)) {
                return PermissionMode.Blacklist;
            }
            if (whitelist || (pmode == PermissionMode.Whitelist && !blacklist)) {
                return PermissionMode.Whitelist;
            }
            return PermissionMode.Default;
        }

        public void SetMode(TKey key, PermissionMode mode) {
            switch (mode) {
                case PermissionMode.Blacklist:
                    Remove(key, NotChar + AllChar);
                    Add(key, AllChar);
                    break;
                case PermissionMode.Whitelist:
                    Remove(key, AllChar);
                    Add(key, NotChar + AllChar);
                    break;
                default:
                    Remove(key, AllChar);
                    Remove(key, NotChar + AllChar);
                    break;
            }
        }

        public Permission Check(TKey key, string value, HashSet<string> @default = null) {
            if (@default != null) {
                var parent = new PermissionDictionary<object>();
                parent.Add("", @default);
                return Check(key, value, () => parent.GetMode(""), x => parent.Check("", x));
            } else {
                return Check(key, value, null, null);
            }
        }

        public Permission Check(TKey key, string value, Func<PermissionMode> parent_Mode, Func<string, Permission> parent_Check) {
            if (value.StartsWith(NotChar)) {
                throw new ArgumentException("Cannot check " + NotChar + " permissions");
            }

            PermissionMode pmode = PermissionMode.Default;
            Permission pcheck = Permission.Default;

            if (parent_Check != null) {
                pmode = parent_Mode();
                pcheck = parent_Check(value);
            }

            PermissionMode mode = GetMode(key, parent_Mode);
            bool accept = Contains(key, value);
            bool reject = Contains(key, NotChar + value);

            switch (mode) {
                case PermissionMode.Blacklist:
                    if (accept) {
                        return Permission.Accept;
                    }
                    if (reject || (pmode != PermissionMode.Whitelist && pcheck == Permission.Reject)) {
                        return Permission.Reject;
                    }
                    return Permission.Accept;
                case PermissionMode.Whitelist:
                    if (reject) {
                        return Permission.Reject;
                    }
                    if (accept || (pmode != PermissionMode.Blacklist && pcheck == Permission.Accept)) {
                        return Permission.Accept;
                    }
                    return Permission.Reject;
                default:
                    if(accept) {
                        return Permission.Accept;
                    }
                    if (reject) {
                        return Permission.Reject;
                    }
                    return pcheck;
            }
        }

        public void Clear(TKey key, string value) {
            if (value == AllChar) {
                // all = clear all values
                Clear();
            } else {
                // single = remove value
                Remove(key, value);
                Remove(key, NotChar + value);
            }
        }

        public void Grant(TKey key, string value) {
            if (value == AllChar) {
                // all = turn to blacklist
                if (ContainsKey(key)) {
                    this[key].RemoveWhere(f => f.StartsWith(NotChar));
                }
                Add(key, AllChar);
            } else {
                // single = add value
                Remove(key, NotChar + value);
                Add(key, value);
            }
        }

        public void Revoke(TKey key, string value) {
            if (value == AllChar) {
                // all = turn to whitelist
                if (ContainsKey(key)) {
                    this[key].RemoveWhere(f => !f.StartsWith(NotChar));
                }
                Add(key, NotChar + AllChar);
            } else {
                // single = add !value
                Remove(key, value);
                Add(key, NotChar + value);
            }
        }

        public void Assign(TKey key, string value) {
            if (value.StartsWith(ClearChar)) {
                Clear(key, value.Substring(1));
            } else {
                if (value.StartsWith(NotChar)) {
                    Revoke(key, value.Substring(1));
                } else {
                    Grant(key, value);
                }
            }
        }
    }

    public enum PermissionMode {
        Default,
        Blacklist,
        Whitelist,
    }

    public enum Permission {
        Default,
        Accept,
        Reject,
    }
}
