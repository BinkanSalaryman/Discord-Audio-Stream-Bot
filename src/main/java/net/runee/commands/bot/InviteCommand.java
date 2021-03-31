package net.runee.commands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

public class InviteCommand extends Command {
    public InviteCommand() {
        super("invite", "Shows this bot's invite link.", CommandCategory.BOT);
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        if(args.length > 0) {
            throw new IncorrectArgCountException(this, ctx);
        }

        // execute
        ctx.reply(new EmbedBuilder()
                .setTitle("Authorize access to your account", DiscordAudioStreamBot.getInstance().getJDA().getInviteUrl(Permission.EMPTY_PERMISSIONS))
                .setDescription("Wanna invite me to your guild?")
                .setColor(Utils.colorYellow)
                .build()
        );
    }
}
