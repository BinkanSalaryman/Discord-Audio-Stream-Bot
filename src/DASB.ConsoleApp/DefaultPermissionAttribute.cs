using System;

namespace DASB {
    [AttributeUsage(AttributeTargets.Method)]
    public class DefaultPermissionAttribute : Attribute {
        public readonly Permission DefaultPermission;

        public DefaultPermissionAttribute(Permission defaultPermission = Permission.Default) {
            this.DefaultPermission = defaultPermission;
        }
    }
}
