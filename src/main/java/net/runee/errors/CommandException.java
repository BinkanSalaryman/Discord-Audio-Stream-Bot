package net.runee.errors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.runee.misc.discord.Command;

public abstract class CommandException extends Exception {
    private Command command;
    private SlashCommandInteractionEvent context;

    public CommandException(Command cmd, SlashCommandInteractionEvent ctx) {
        this.command = cmd;
        this.context = ctx;
    }

    public Command getCommand() {
        return command;
    }

    public SlashCommandInteractionEvent getContext() {
        return context;
    }

    public abstract String getReplyMessage();
}
