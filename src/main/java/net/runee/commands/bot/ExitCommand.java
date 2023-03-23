package net.runee.commands.bot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.errors.InsufficientPermissionsException;
import net.runee.gui.MainFrame;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class ExitCommand extends Command {
    public ExitCommand() {
        super(Commands.slash("exit", "Terminate the bot program"));
        _public = true;
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws InsufficientPermissionsException {
        ensureOwnerPermission(ctx);
        reply(ctx, "0xDEADBEEF", Utils.colorGreen);
        ctx.getJDA().shutdownNow();
        MainFrame.getInstance().dispose();
        System.exit(0);
    }
}
