package net.runee.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class AboutCommand extends Command {
    public AboutCommand() {
        this.name = "about";
        this.help = "Shows information about this software.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        if(args.length > 0) {
            throw new IncorrectArgCountException(this, ctx);
        }

        // execute
        ctx.reply(new EmbedBuilder()
                .setTitle(DiscordAudioStreamBot.NAME, DiscordAudioStreamBot.GITHUB_URL)
                .setDescription("A simple discord audio streaming bot.")
                .setColor(Utils.colorYellow)
                .build()
        );
    }
}
