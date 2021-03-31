package net.runee.commands.bot;

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
        super("help", "Shows information about command if specified, or else lists all available commands.", CommandCategory.BOT);
        this.arguments.add(new Argument("command", "Command in question", "Command name", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        final List<Command> allCommands = DiscordAudioStreamBot.getInstance().getCommands();
        switch (args.length) {
            case 0: {
                showAvailableCommands(ctx, allCommands);
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

                showManual(ctx, command);
                break;
            }
            default:
                throw new IncorrectArgCountException(this, ctx);
        }
    }

    private void showAvailableCommands(CommandContext ctx, List<Command> allCommands) {
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
            if (!commands.isEmpty()) {
                String commandsHelpStr = Utils.ucListItem + commands.stream().map(HelpCommand::flatUsage).collect(Collectors.joining("\n" + Utils.ucListItem));
                embed.addField(category.getKey().getDisplayName(), commandsHelpStr, false);
            }
        }
        ctx.reply(embed.build());
    }

    private void showManual(CommandContext ctx, Command command) {
        List<Argument> arguments = command.getArguments();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Manual for `" + command.getName() + "`")
                .setDescription(
                        "**Summary:** " + command.getSummary()
                                + "\n**Usage:** " + flatUsage(command)
                                + "\n(must replace *italic* argument names with appropriate values)"
                )
                .setColor(Utils.colorYellow);
        for (Argument argument : arguments) {
            String argText = "Summary: " + argument.getSummary();
            argText += "\nType: " + (
                    argument.isEnum()
                    ? "Any of: " + Arrays.stream(argument.getEnumValues()).map(arg -> "**" + arg + "**").collect(Collectors.joining(", "))
                    : formatArgumentType(argument.getType())
            );
            argText += "\nRequired: " + (argument.isOptional() ? "No" : "**Yes**");
            embed.addField("Argument `" + argument.getName() + "`", argText, true);
        }

        ctx.reply(embed.build());
    }

    private static String flatUsage(Command cmd) {
        String result = "`" + cmd.getName() + "`";
        final List<Argument> args = cmd.getArguments();
        if (!args.isEmpty()) {
            result += " " + args.stream().map(arg -> "*`" + arg + "`*").collect(Collectors.joining(" "));
        }
        return result;
    }

    private static String formatArgumentType(String type) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < type.length(); i++) {
            final char c = type.charAt(i);
            if (Character.isUpperCase(c) && result.length() > 0) {
                result.append(" ");
            }
            result.append(c);
        }
        return result.toString();
    }
}
