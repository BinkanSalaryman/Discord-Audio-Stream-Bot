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
            var uncheckedPermissions = permissions.Split(' ');
            var invalidPermissions = new List<String>();
            var validPermissions = new List<String>();
            foreach (var permission in permissions.Split(' ')) {
                if(isValidPermissionEntry(permission)) {
                    validPermissions.Add(permission);
                } else {
                    invalidPermissions.Add(permission);
                }
            }

            if (invalidPermissions.Count > 0) {
                await ReplyAsync(Say(BotString.warning_unrecognizedPermissionEntries, string.Join(", ", invalidPermissions.Select(p => "`"+p+"`"))));
            }

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

                    foreach (var permission in validPermissions) {
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

            foreach (var command in validPermissions) {
                Context.Bot.Config.commandsUserPermissions.Assign(user.Id, command);
            }
            Context.Bot.UpdateConfig();
        }

        private static bool isValidPermissionEntry(string permissionEntry) {
            string commandKey;
            if (permissionEntry.StartsWith("!") || permissionEntry.StartsWith("~")) {
                commandKey = permissionEntry.Substring(1);
            } else {
                commandKey = permissionEntry;
            }
            if(commandKey == "*") {
                return true;
            }
            var commandService = (CommandService)AudioStreamBot.Services.GetService(typeof(CommandService));
            foreach (var command in commandService.Commands) {
                if (GetCommandKey(command) == commandKey) {
                    return true;
                }
            }
            return false;
        }

        [Command("syntax")]
        [Summary("Shows detailed information about the permission syntax.")]
        [DisablePermissionCheck]
        public async Task HelpPermissionsSyntax() {
            string description = ""
                + "**Permission entry:**\n"
                + "\n"
                + "A permission entry is either a command key with a modifying prefix, or one the special symbols described later.\n"
                + "**Modifying prefixes:**\n"
                + "\t * Accept (no prefix) - Grants a permission.\n"
                + "\t * Reject (`!` prefix) - Revokes a permission.\n"
                + "\t * Clear (`~`) - Resets a permission, which will then take the default value instead.\n"
                + "**Special symbols:**\n"
                + "\t * Accept all/Blacklist (`*`) - Grants all permissions, except explicitly rejected ones. Effectively turns the permission entries into a blacklist.\n"
                + "\t * Reject all/Whitelist (`!*`) - Revokes all permissions, except explicitly accepted ones. Effectively turns the permission entries into a whitelist.\n"
                + "\t * Clear all/Use default (`~*`) - Resets all permissions, which will then take the default values instead.\n"
                + "\n"
                + "\n"
                + "**Command key:**\n"
                + "\n"
                + "A command key is a full qualified identifier to a command handler in code. It consists of the following parts:\n"
                + "\t * Module name(s) - Full path of modules of the command, separated by `.`. Should consist of only one module in most cases, though.\n"
                + "\t * Command name - The name of the command.\n"
                + "\t * Command parameters - Parameter names list separated by `,`, surrounded with `(` `)`.\n"
                + "\n"
                + "Example command key: `aParentModule.aChildModule.aCommand(arg1,arg2,arg3)`"
            ;
            await ReplyAsync("", embed: new EmbedBuilder()
                .WithTitle("Permission entry & command key syntax")
                .WithColor(Utils.color_info)
                .WithDescription(description)
            );
        }
    }
}
