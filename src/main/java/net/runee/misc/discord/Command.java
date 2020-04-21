package net.runee.misc.discord;

import net.runee.errors.CommandException;

public abstract class Command {
    protected String name;
    protected String help = "";
    protected String arguments = "";
    protected CommandCategory category = CommandCategory.GENERAL;

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public String getArguments() {
        return arguments;
    }

    public CommandCategory getCategory() {
        return category;
    }

    public abstract void execute(CommandContext ctx, String... args) throws CommandException;
}
