package net.runee.commands.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class LeaveAudioCommand extends Command {
    public LeaveAudioCommand() {
        super(Commands.slash("leave", "Leave from the guilds audio instance"));
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        Guild guild = ensureAdminOrOwnerPermission(ctx);

        AudioChannel channel = guild.getAudioManager().getConnectedChannel();
        if (channel == null) {
            reply(ctx, "Not connected - join an audio channel first!", Utils.colorRed);
            return;
        }
        DiscordAudioStreamBot.getInstance().leaveAudio(guild);
        reply(ctx, "Left audio channel `" + channel.getName() + "`.", Utils.colorGreen);
    }
}
