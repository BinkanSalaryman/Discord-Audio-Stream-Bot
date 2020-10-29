package net.runee.commands;

import net.runee.errors.CommandException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class ExitCommand extends Command {
    public ExitCommand() {
        this.name = "exit";
        this.summary = "Terminates the bot program.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        if(ctx.getGuild() != null) {
            ctx.ensureAdminPermission();
        } else {
            ctx.ensureOwnerPermission();
        }
        ctx.replySuccess("0xDEADBEEF");
        ctx.getJDA().shutdown();
        System.exit(0);
    }
}
