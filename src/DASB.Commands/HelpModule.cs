using Discord;
using Discord.Commands;
using System;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    [Group("help")]
    public class HelpModule : CommandsModule {
        private readonly CommandService _commands;
        private readonly IServiceProvider _map;

        public HelpModule(IServiceProvider map, CommandService commands) {
            _commands = commands;
            _map = map;
        }

        [Command("commands"), Alias("")]
        [Summary("Lists this bot's commands.")]
        [DefaultPermission(Permission.Accept)]
        public async Task Help(string path = "") {
            EmbedBuilder output = new EmbedBuilder();
            if (path == "") {
                output.Title = Say(BotString.info_botServiceTitle);

                foreach (var mod in _commands.Modules.Where(m => m.Parent == null)) {
                    AddHelp(mod, ref output);
                }

                output.Footer = new EmbedFooterBuilder {
                    Text = "Use 'help <module>' to get help with a module."
                };
            } else {
                var mod = _commands.Modules.FirstOrDefault(m => m.Name.Replace("Module", "").ToLower() == path.ToLower());
                if (mod == null) { await ReplyAsync("No module could be found with that name."); return; }

                output.Title = mod.Name;
                output.Description = $"{mod.Summary}\n" +
                (!string.IsNullOrEmpty(mod.Remarks) ? $"({mod.Remarks})\n" : "") +
                (mod.Aliases.Any() ? $"Prefix(es): {string.Join(",", mod.Aliases)}\n" : "") +
                (mod.Submodules.Any() ? $"Submodules: {mod.Submodules.Select(m => m.Name)}\n" : "") + " ";
                AddCommands(mod, ref output);
            }

            await ReplyAsync("", embed: output);
        }

        public void AddHelp(ModuleInfo module, ref EmbedBuilder builder) {
            foreach (var sub in module.Submodules) {
                AddHelp(sub, ref builder);
            }
            builder.AddField(f => {
                f.Name = $"**{module.Name}**";
                f.Value = $"Submodules: {string.Join(", ", module.Submodules.Select(m => m.Name))}" +
                $"\n" +
                $"Commands: {string.Join(", ", module.Commands.Select(x => $"`{x.Name}`"))}";
            });
        }

        public void AddCommands(ModuleInfo module, ref EmbedBuilder builder) {
            foreach (var command in module.Commands) {
                command.CheckPreconditionsAsync(Context, _map).GetAwaiter().GetResult();
                AddCommand(command, ref builder);
            }
        }

        public void AddCommand(CommandInfo command, ref EmbedBuilder builder) {
            builder.AddField(f => {
                f.Name = $"**{command.Name}**";
                f.Value = $"{command.Summary}\n" +
                (!string.IsNullOrEmpty(command.Remarks) ? $"({command.Remarks})\n" : "") +
                (command.Aliases.Any() ? $"**Aliases:** {string.Join(", ", command.Aliases.Select(x => $"`{x}`"))}\n" : "") +
                $"**Usage:** `{GetPrefix(command)} {GetParams(command)}`";
            });
        }

        public string GetParams(CommandInfo command) {
            StringBuilder output = new StringBuilder();
            if (command.Parameters.Any()) {
                foreach (var param in command.Parameters) {
                    if (param.IsOptional)
                        output.Append($"[{param.Name} = {param.DefaultValue}] ");
                    else if (param.IsMultiple)
                        output.Append($"|{param.Name}| ");
                    else if (param.IsRemainder)
                        output.Append($"...{param.Name} ");
                    else
                        output.Append($"<{param.Name}> ");
                }
            }
            return output.ToString();
        }

        public string GetPrefix(CommandInfo command) {
            if (command.Aliases.Contains("")) {
                return "";
            } else {
                return command.Aliases.FirstOrDefault();
            }
        }
    }
}
