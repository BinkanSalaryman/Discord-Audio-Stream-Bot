package net.runee.commands;

import net.runee.errors.CommandException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class StopCommand extends Command {
    public StopCommand() {
        this.name = "stop";
        this.summary = "Stops the bot.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureOwnerPermission();
        ctx.replySuccess("0xDEADBEEF");
        ctx.getJDA().shutdown();
    }
}
