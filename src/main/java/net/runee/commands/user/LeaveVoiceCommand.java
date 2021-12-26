package net.runee.commands.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class LeaveVoiceCommand extends Command {
    public LeaveVoiceCommand() {
        super(new CommandData("leave", "Leave from the guilds voice instance"));
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        Guild guild = ensureAdminOrOwnerPermission(ctx);

        VoiceChannel channel = guild.getAudioManager().getConnectedChannel();
        if (channel == null) {
            reply(ctx, "Not connected - join a voice channel first!", Utils.colorRed);
            return;
        }
        DiscordAudioStreamBot.getInstance().leaveVoice(guild);
        reply(ctx, "Left voice channel `" + channel.getName() + "`.", Utils.colorGreen);
    }
}
