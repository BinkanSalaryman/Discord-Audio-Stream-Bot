package net.runee.commands.bot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class LeaveAudioAllCommand extends Command {
    public LeaveAudioAllCommand() {
        super(Commands.slash("leave-all", "Leave from all guild audio instances"));
        _public = true;
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        ensureOwnerPermission(ctx);

        // execute
        DiscordAudioStreamBot.getInstance().leaveVoiceAll();
        reply(ctx, "Left all audio channels.", Utils.colorGreen);
    }
}
