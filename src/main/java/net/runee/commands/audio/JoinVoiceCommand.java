package net.runee.commands.audio;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
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

public class JoinVoiceCommand extends Command {
    public JoinVoiceCommand() {
        this.name = "join";
        this.arguments = "[channel]";
        this.help = "Joins a voice channel.";
        this.category = CommandCategory.AUDIO;
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        final Guild guild = ctx.ensureGuildContext();
        VoiceChannel channel;
        switch (args.length) {
            case 0: {
                // join automatically
                Member author = guild.getMember(ctx.getAuthor());
                List<VoiceChannel> channelMatches = Utils.findSuitableVoiceChannel(guild, author);
                switch (channelMatches.size()) {
                    case 0:
                        ctx.replyWarning("No voice channel found!");
                        return;
                    default:
                        channel = channelMatches.get(0);
                        break;
                }
                break;
            }
            case 1: {
                // join specific
                String channelSearch = args[0];
                List<VoiceChannel> channelMatches = Utils.findVoiceChannel(guild, channelSearch);
                switch (channelMatches.size()) {
                    case 0:
                        ctx.replyWarning("Can't find specified voice channel!");
                        return;
                    case 1:
                        channel = channelMatches.get(0);
                        break;
                    default:
                        ctx.replyWarning("There are multiple voice channels with that name!");
                        return;
                }
                break;
            }
            default:
                throw new IncorrectArgCountException(this, ctx);
        }


        // execute
        if (guild.getAudioManager().isAttemptingToConnect()) {
            ctx.replyWarning("The bot is already trying to connect! Enter the chill zone!");
            return;
        }

        Member self = guild.getSelfMember();
        if (!self.hasPermission(Permission.VOICE_CONNECT)) {
            // missing permissions
            ctx.replyWarning("Insufficient permissions to join a voice channel!");
            return;
        }

        DiscordAudioStreamBot.getInstance().joinVoice(channel);
        ctx.replySuccess("Joined voice channel `" + channel.getName() + "`.");
    }
}
