package net.runee.errors;

import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public abstract class CommandException extends Exception {
    private Command command;
    private CommandContext context;

    public CommandException(Command command, CommandContext context) {
        this.command = command;
        this.context = context;
    }

    public Command getCommand() {
        return command;
    }

    public CommandContext getContext() {
        return context;
    }

    public abstract String getReplyMessage();
}
