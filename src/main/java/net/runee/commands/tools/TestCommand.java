package net.runee.commands.tools;

import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class TestCommand extends Command {
    public TestCommand() {
        name = "test";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        if(args.length != 0) {
            throw new IncorrectArgCountException(this, ctx);
        }

        ctx.replySuccess("OK");
    }
}
