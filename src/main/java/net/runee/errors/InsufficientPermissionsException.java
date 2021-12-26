package net.runee.errors;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.runee.misc.discord.Command;

public class InsufficientPermissionsException extends CommandException {
    public InsufficientPermissionsException(Command cmd, SlashCommandEvent ctx) {
        super(cmd, ctx);
    }

    @Override
    public String getReplyMessage() {
        return "Insufficient permissions to run command";
    }
}
