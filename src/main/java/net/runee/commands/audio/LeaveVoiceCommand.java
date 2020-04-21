package net.runee.commands.audio;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.BassException;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

import java.util.List;

public class LeaveVoiceCommand extends Command {
    public LeaveVoiceCommand() {
        this.name = "leave";
        this.arguments = "[guild]";
        this.help = "Leaves from a guild voice instance.";
        this.category = CommandCategory.AUDIO;
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        Guild guild;
        switch (args.length) {
            case 0: {
                // leave automatically
                guild = ctx.ensureGuildContext();
                break;
            }
            case 1: {
                // leave specific
                String guildSearch = args[0];
                List<Guild> guildMatches = Utils.findGuild(ctx.getJDA(), guildSearch);
                switch (guildMatches.size()) {
                    case 0:
                        ctx.replyWarning("No such server!");
                        return;
                    case 1:
                        guild = guildMatches.get(0);
                        break;
                    default:
                        ctx.replyWarning("There are multiple servers with that name!");
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
