package net.runee.commands.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.DataMessage;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

public class MessageCommand extends Command {
    public MessageCommand() {
        this.name = "msg";
        this.arguments = "user message";
        this.summary = "Sends a private message to a user.";
        this.category = CommandCategory.TOOLS;
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        if(args.length != 2) {
            throw new IncorrectArgCountException(this, ctx);
        }
        String userSearch = args[0];
        Long userId = Utils.tryParseLong(userSearch);
        if(userId == null) {
            ctx.replyWarning("User must be provided by id!");
            return;
        }
        User user = ctx.getJDA().getUserById(userId);
        if(user == null) {
            ctx.replyWarning("User not found!");
            return;
        }
        if(user.isBot()) {
            ctx.replyWarning("You may not message bots by command!");
            return;
        }
        String message = args[1];
        DiscordAudioStreamBot.getInstance().sendDirect(user, new DataMessage(false, "You received a message:", null, new EmbedBuilder()
                .setAuthor(ctx.getAuthor().getName(), null, ctx.getAuthor().getAvatarUrl())
                .setDescription(message)
                .setColor(Utils.colorYellow)
                .build()
        ));
        ctx.replySuccess("Message sent.");
    }
}
