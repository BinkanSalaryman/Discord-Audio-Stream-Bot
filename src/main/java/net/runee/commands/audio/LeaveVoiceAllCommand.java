package net.runee.commands.audio;

import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

public class LeaveVoiceAllCommand extends Command {
    public LeaveVoiceAllCommand() {
        this.name = "leave-all";
        this.summary = "Leaves from all server voice instances.";
        this.category = CommandCategory.AUDIO;
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureOwnerPermission();

        // parse args
        if (args.length > 0) {
            throw new IncorrectArgCountException(this, ctx);
        }

        // execute
        DiscordAudioStreamBot.getInstance().leaveVoiceAll();
        ctx.replySuccess("Left all voice channels.");
    }
}
