using Discord.Commands;
using System;
using System.Threading.Tasks;

namespace DASB {
    [AttributeUsage(AttributeTargets.Method)]
    public class RequireGuildContextAttribute : Attribute {
        public readonly bool RequiresGuildContext;

        public RequireGuildContextAttribute(bool requiresGuildContext = true) {
            this.RequiresGuildContext = requiresGuildContext;
        }
    }
}
