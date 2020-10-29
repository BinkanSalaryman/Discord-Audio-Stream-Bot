package net.runee.commands;

import net.dv8tion.jda.api.entities.Activity;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;

public class ActivityCommand extends Command {
    public ActivityCommand() {
        this.name = "activity";
        this.arguments = "activity:playing|streaming|listening|watching text [url]";
        this.summary = "Manages the bot users' activity.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureAdminPermission();

        // parse args
        if (args.length < 2) {
            throw new IncorrectArgCountException(this, ctx);
        }
        String activity = args[0].toLowerCase();
        String text = args[1];
        String url = null;

        switch (activity) {
            case "playing":
            case "listening":
            case "watching":
                if(args.length != 2) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                break;
            case "streaming":
                if(args.length != 3) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                url = args[2];
                if(!Activity.isValidStreamingUrl(url)) {
                    ctx.replyWarning("Invalid streaming url.");
                    return;
                }
                break;
            default:
                ctx.replyWarning("Unrecognized activity: `" + activity + "`.");
                return;
        }

        // execute
        switch (activity) {
            case "playing":
                ctx.getJDA().getPresence().setActivity(Activity.playing(text));
                break;
            case "streaming":
                ctx.getJDA().getPresence().setActivity(Activity.streaming(text, url));
                break;
            case "listening":
                ctx.getJDA().getPresence().setActivity(Activity.listening(text));
                break;
            case "watching":
                ctx.getJDA().getPresence().setActivity(Activity.watching(text));
                break;
            default:
                ctx.replyWarning("Unrecognized activity: `" + activity + "`.");
                return;
        }
        ctx.replySuccess("Activity updated.");
    }
}
