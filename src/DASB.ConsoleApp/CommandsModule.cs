using Discord;
using Discord.Commands;
using Discord.WebSocket;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.NetworkInformation;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    public class CommandsModule : ModuleBase<CommandContext> {
        protected Permission CheckUserPermissions(ulong userId, string commandKey) {
            if (userId == Context.Bot.AppInfo.Owner.Id) {
                return Permission.Accept;
            }
            if (Context.Guild != null) {
                Permission rolePermission = checkRolePermissions(userId, commandKey, 0);
                if (rolePermission != Permission.Default) {
                    return rolePermission;
                }
            }

            return Context.Bot.Config.commandsUserPermissions.Check(userId, commandKey, Context.Bot.Config.commandsDefaultPermissions);
        }

        protected Permission CheckRolePermissions(ulong roleId, string commandKey) {
            return checkRolePermissions(roleId, commandKey, 1);
        }

        private Permission checkRolePermissions(ulong entityId, string commandKey, int entityType) {
            PermissionDictionary<ulong> rolePermissions;
            if (Context.Bot.Config.commandsRolePermissions.TryGetValue(Context.Guild.Id, out rolePermissions)) {
                IEnumerable<SocketRole> roles;
                switch (entityType) {
                    case 0: {
                            var user = Context.Guild.GetUser(entityId);
                            roles = user.Roles.OrderByDescending(r => r.Position);
                            break;
                        }
                    case 1: {
                            var role_ = Context.Guild.GetRole(entityId);
                            roles = Context.Guild.Roles.OrderByDescending(r => r.Position).Where(r => r.Position <= role_.Position);
                            break;
                        }
                    default:
                        throw new ArgumentOutOfRangeException();
                }
                foreach (var role in roles) {
                    Permission rolePermission = rolePermissions.Check(role.Id, commandKey, Context.Bot.Config.commandsDefaultPermissions);
                    if (rolePermission != Permission.Default) {
                        return rolePermission;
                    }
                }
            }
            return Permission.Default;
        }

        protected string Say(BotString @string) {
            return Context.Bot.Agent.Say(@string);
        }

        protected string Say(BotString @string, params object[] args) {
            return Context.Bot.Agent.Say(@string, args);
        }

        protected string Say(string @string) {
            return Context.Bot.Agent.Say(@string);
        }

        protected string Say(string @string, params object[] args) {
            return Context.Bot.Agent.Say(@string, args);
        }

        protected override void BeforeExecute(CommandInfo command) {
            var dpc = command.Attributes.OfType<DisablePermissionCheckAttribute>().SingleOrDefault();
            bool checkPermissions = dpc == null;

            if (checkPermissions && CheckUserPermissions(Context.Message.Author.Id, Utils.GetCommandKey(command)) != Permission.Accept) {
                throw new InvalidOperationException(nameof(DisablePermissionCheckAttribute));
            }

            var rgc = command.Attributes.OfType<RequireGuildContextAttribute>().SingleOrDefault();
            bool checkGuildContext = rgc != null && rgc.RequiresGuildContext;

            if (checkGuildContext && Context.Guild == null) {
                throw new InvalidOperationException(nameof(RequireGuildContextAttribute));
            }
        }
    }
}
