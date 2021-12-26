package net.runee.commands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.errors.InsufficientPermissionsException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class StopCommand extends Command {
    public StopCommand() {
        super(new CommandData("stop", "Stop the bot by logging off"));
        _public = true;
    }

    @Override
    public void run(SlashCommandEvent ctx) throws InsufficientPermissionsException {
        ensureOwnerPermission(ctx);
        reply(ctx, "0xDEADBEEF", Utils.colorGreen);
        ctx.getJDA().shutdown();
    }
}
