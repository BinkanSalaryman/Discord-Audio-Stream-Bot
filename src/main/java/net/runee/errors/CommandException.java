package net.runee.errors;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.runee.misc.discord.Command;

public abstract class CommandException extends Exception {
    private Command command;
    private SlashCommandEvent context;

    public CommandException(Command cmd, SlashCommandEvent ctx) {
        this.command = cmd;
        this.context = ctx;
    }

    public Command getCommand() {
        return command;
    }

    public SlashCommandEvent getContext() {
        return context;
    }

    public abstract String getReplyMessage();
}
