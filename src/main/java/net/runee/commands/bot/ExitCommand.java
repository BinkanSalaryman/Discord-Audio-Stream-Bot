package net.runee.commands.bot;

import net.runee.errors.CommandException;
import net.runee.gui.MainFrame;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

public class ExitCommand extends Command {
    public ExitCommand() {
        super("exit", "Terminates the bot program.", CommandCategory.BOT);
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureOwnerPermission();
        ctx.replySuccess("0xDEADBEEF");
        ctx.getJDA().shutdownNow();
        MainFrame.getInstance().dispose();
        System.exit(0);
    }
}
