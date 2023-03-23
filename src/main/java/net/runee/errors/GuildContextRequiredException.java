package net.runee.errors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.runee.misc.discord.Command;

public class GuildContextRequiredException extends CommandException {
    public GuildContextRequiredException(Command cmd, SlashCommandInteractionEvent ctx) {
        super(cmd, ctx);
    }

    @Override
    public String getReplyMessage() {
        return "Please issue this command in a guild channel!";
    }
}
