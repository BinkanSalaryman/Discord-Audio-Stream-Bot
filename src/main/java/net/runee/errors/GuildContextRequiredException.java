package net.runee.errors;

import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class GuildContextRequiredException extends CommandException {
    public GuildContextRequiredException(Command command, CommandContext context) {
        super(command, context);
    }

    @Override
    public String getReplyMessage() {
        return "Please issue this command in a guild channel!";
    }
}
