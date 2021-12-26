package net.runee.errors;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.runee.misc.discord.Command;

public class MissingOptionException extends CommandException {
    private final String optionName;

    public MissingOptionException(Command cmd, SlashCommandEvent ctx, String optionName) {
        super(cmd, ctx);
        this.optionName = optionName;
    }

    @Override
    public String getReplyMessage() {
        return "Missing option \"" + optionName + "\"";
    }
}
