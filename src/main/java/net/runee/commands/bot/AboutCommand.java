package net.runee.commands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class AboutCommand extends Command {
    public AboutCommand() {
        super(Commands.slash("about", "Show information about this software"));
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        reply(ctx,
                DiscordAudioStreamBot.NAME, DiscordAudioStreamBot.GITHUB_URL,
                "A simple discord audio streaming bot.",
                Utils.colorYellow
        );
    }
}
