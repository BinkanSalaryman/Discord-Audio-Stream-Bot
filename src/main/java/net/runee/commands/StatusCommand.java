package net.runee.commands;

import net.dv8tion.jda.api.OnlineStatus;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class StatusCommand extends Command {
    public StatusCommand() {
        this.name = "status";
        this.arguments = "status:online|idle|dnd|inv";
        this.summary = "Manages the bot users' online status.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        if (args.length != 1) {
            throw new IncorrectArgCountException(this, ctx);
        }
        String status = args[0].toLowerCase();

        // execute
        switch (status) {
            case "online":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
                break;
            case "idle":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
                break;
            case "dnd":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                break;
            case "inv":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.INVISIBLE);
                break;
            default:
                ctx.replyWarning("Unrecognized status: `" + status + "`.");
                return;
        }
        ctx.replySuccess("Status updated.");
    }
}
