package net.runee.commands.botuser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

import java.util.List;

public class LeaveGuildCommand extends Command {
    public LeaveGuildCommand() {
        super("leave-guild", "Lets the bot user leave a guild.", CommandCategory.BOT_USER);
        this.arguments.add(new Argument("guild", "Guild in question", "Guild", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        Guild guild;
        switch (args.length) {
            case 0:
                // leave current
                guild = ctx.ensureAdminOrOwnerPermission();
                break;
            case 1: {
                // leave specific
                ctx.ensureOwnerPermission();
                List<Guild> guildMatches = Utils.findGuild(ctx.getJDA(), args[0]);
                switch (guildMatches.size()) {
                    case 0:
                        ctx.replyWarning("No such guild!");
                        return;
                    case 1: {
                        guild = guildMatches.get(0);
                        Member authorAsMember = guild.getMember(ctx.getAuthor());
                        if (authorAsMember == null) {
                            ctx.ensureOwnerPermission();
                        } else {
                            guild = ctx.ensureAdminOrOwnerPermission();
                        }
                        break;
                    }
                    default:
                        ctx.replyWarning("There are multiple guilds with that name!");
                        return;
                }
                break;
            }
            default:
                throw new IncorrectArgCountException(this, ctx);
        }
        guild.leave().queue(ignore -> {
            ctx.replySuccess("Left guild.");
        });

    }
}
