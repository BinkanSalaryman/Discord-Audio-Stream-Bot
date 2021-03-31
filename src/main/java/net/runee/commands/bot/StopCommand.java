package net.runee.commands.bot;

import net.runee.errors.CommandException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop", "Stops the bot.", CommandCategory.BOT);
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureOwnerPermission();
        ctx.replySuccess("0xDEADBEEF");
        ctx.getJDA().shutdown();
    }
}
