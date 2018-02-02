using Discord;
using Discord.Commands;
using Discord.WebSocket;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    [Group("permissions")]
    public class PermissionsModule : CommandsModule {
        [Command("assign")]
        [Summary("Modifies command *permissions* for a *user*, or users with *role*. Use `permissions syntax` to get help with permission syntax.")]
        [DefaultPermission(Permission.Reject)]
        public async Task AssignPermissions(string user_or_role, [Remainder] string permissions) {
            if (Context.Guild != null) {
                // assign role permissions
                var role = Utils.ParseRole(Context.Guild, user_or_role);
                if (role != null) {
                    PermissionDictionary<ulong> permissions_;
                    if (Context.Bot.Config.commandsRolePermissions.ContainsKey(Context.Guild.Id)) {
                        permissions_ = Context.Bot.Config.commandsRolePermissions[Context.Guild.Id];
                    } else {
                        permissions_ = new PermissionDictionary<ulong>();
                        Context.Bot.Config.commandsRolePermissions.Add(Context.Guild.Id, permissions_);
                    }

                    foreach (var permission in permissions.Split(' ')) {
                        permissions_.Assign(role.Id, permission);
                    }
                    Context.Bot.UpdateConfig();
                    return;
                }
            }

            // assign user permissions
            var user = Utils.ParseUser(Context.Discord, user_or_role);
            if (user == null) {
                await ReplyAsync(Say(BotString.warning_badUser));
                return;
            }

            foreach (var command in permissions.Split(' ')) {
                Context.Bot.Config.commandsUserPermissions.Assign(user.Id, command);
            }
            Context.Bot.UpdateConfig();
        }

        [Command("syntax")]
        [Summary("Shows detailed information about the permission syntax.")]
        public async Task HelpPermissionsSyntax() {
            throw new NotImplementedException();
        }
    }
}
