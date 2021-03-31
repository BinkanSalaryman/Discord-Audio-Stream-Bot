package net.runee.commands.botuser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

import java.util.List;

public class LeaveVoiceCommand extends Command {
    public LeaveVoiceCommand() {
        super("leave", "Leaves from a guild voice instance.", CommandCategory.BOT_USER);
        this.arguments.add(new Argument("guild", "Guild instance in question", "Guild", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        Guild guild;
        switch (args.length) {
            case 0: {
                // leave automatically
                guild = ctx.ensureAdminOrOwnerPermission();
                break;
            }
            case 1: {
                // leave specific
                String guildSearch = args[0];
                List<Guild> guildMatches = Utils.findGuild(ctx.getJDA(), guildSearch);
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

        // execute
        VoiceChannel channel = guild.getAudioManager().getConnectedChannel();
        if (channel == null) {
            ctx.replyWarning("Not connected - join a voice channel first!");
            return;
        }
        DiscordAudioStreamBot.getInstance().leaveVoice(guild);
        ctx.replySuccess("Left voice channel `" + channel.getName() + "`.");
    }
}
