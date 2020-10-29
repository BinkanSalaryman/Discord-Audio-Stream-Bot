package net.runee.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

import java.util.*;
import java.util.stream.Collectors;

public class HelpCommand extends Command {
    public HelpCommand() {
        this.name = "help";
        this.arguments = "[command]";
        this.summary = "Shows information about specified command, or else lists all commands.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        final List<Command> allCommands = DiscordAudioStreamBot.getInstance().getCommands();
        switch (args.length) {
            case 0: {
                Map<CommandCategory, List<Command>> categorizedCommands = new LinkedHashMap<>();
                for (CommandCategory category : CommandCategory.values()) {
                    categorizedCommands.put(category, new ArrayList<>());
                }
                for (Command cmd : allCommands.stream().sorted(Comparator.comparing(Command::getName)).collect(Collectors.toList())) {
                    categorizedCommands.get(cmd.getCategory()).add(cmd);
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Available commands:")
                        .setColor(Utils.colorYellow);
                for (Map.Entry<CommandCategory, List<Command>> category : categorizedCommands.entrySet()) {
                    List<Command> commands = category.getValue();
                    if(!commands.isEmpty()) {
                        String commandsHelpStr = Utils.ucListItem + commands.stream().map(this::formatSyntax).collect(Collectors.joining("\n" + Utils.ucListItem));
                        embed.addField(formatCategory(category.getKey()), commandsHelpStr, false);
                    }
                }
                ctx.reply(embed.build());
                break;
            }
            case 1: {
                String commandSearch = args[0];
                Command command = null;
                for (Command cmd : allCommands) {
                    if (Objects.equals(cmd.getName(), commandSearch)) {
                        command = cmd;
                        break;
                    }
                }
                if (command == null) {
                    ctx.replyWarning("No such command!");
                    return;
                }

                ctx.reply(new EmbedBuilder()
                        .setTitle("Help page for `" + command.getName() + "`:")
                        .setDescription(command.getSummary())
                        .addField("Syntax", formatSyntax(command), false)
                        .setColor(Utils.colorYellow)
                        .build()
                );
                break;
            }
            default:
                throw new IncorrectArgCountException(this, ctx);
        }
    }

    private String formatSyntax(Command cmd) {
        String result = "**`" + cmd.getName() + "`**";
        String argStr = cmd.getArguments();
        if (!argStr.isEmpty()) {
            String[] args = argStr.split(" ");
            result = result + " `" + String.join("` `", args) + "`";
        }
        return result;
    }

    private String formatCategory(CommandCategory category) {
        String[] words = category.name().replace("_", " ").split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            words[i] = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }
        return String.join(" ", words);
    }
}
