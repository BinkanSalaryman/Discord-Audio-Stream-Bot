package net.runee.commands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class AboutCommand extends Command {
    public AboutCommand() {
        super(new CommandData("about", "Show information about this software"));
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        ctx.replyEmbeds(new EmbedBuilder()
                        .setTitle(DiscordAudioStreamBot.NAME, DiscordAudioStreamBot.GITHUB_URL)
                        .setDescription("A simple discord audio streaming bot.")
                        .setColor(Utils.colorYellow)
                        .build()
                )
                .setEphemeral(!_public)
                .queue()
        ;
    }
}
