package net.runee.errors;

import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class InsufficientPermissionsException extends CommandException {
    public InsufficientPermissionsException(Command command, CommandContext context) {
        super(command, context);
    }

    @Override
    public String getReplyMessage() {
        return "Insufficient permissions to run command";
    }
}
