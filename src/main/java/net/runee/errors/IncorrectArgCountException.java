package net.runee.errors;

import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class IncorrectArgCountException extends CommandException {
    public IncorrectArgCountException(Command command, CommandContext context) {
        super(command, context);
    }

    @Override
    public String getReplyMessage() {
        return "Incorrect number of arguments provided!";
    }
}
