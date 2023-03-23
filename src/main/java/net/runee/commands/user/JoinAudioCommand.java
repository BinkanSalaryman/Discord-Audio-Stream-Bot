package net.runee.commands.user;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

import java.util.List;

public class JoinAudioCommand extends Command {
    public JoinAudioCommand() {
        super(Commands.slash("join", "Join an audio channel"));
        data.addOption(OptionType.CHANNEL, "channel", "Audio channel in question", false);
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        // parse args
        final Guild guild = ensureAdminOrOwnerPermission(ctx);

        OptionMapping channelOpt = ctx.getOption("channel");
        AudioChannel channel;
        if (channelOpt != null) {
            // join specific
            GuildChannelUnion guildChannel = ensureOptionPresent(ctx, "channel").getAsChannel();
            if (guildChannel instanceof AudioChannel) {
                channel = guildChannel.asAudioChannel();
            } else {
                reply(ctx, "Channel is not an audio channel", Utils.colorRed);
                return;
            }
        } else {
            // join automatically
            List<AudioChannel> channelMatches = Utils.findSuitableAudioChannel(guild, ctx.getMember());
            switch (channelMatches.size()) {
                case 0:
                    reply(ctx, "No audio channel found!", Utils.colorRed);
                    return;
                default:
                    channel = channelMatches.get(0);
                    break;
            }
        }


        // execute
        Member self = guild.getSelfMember();
        if (!self.hasPermission(Permission.VOICE_CONNECT)) {
            // missing permissions
            reply(ctx, "Insufficient permissions to join an audio channel!", Utils.colorRed);
            return;
        }

        DiscordAudioStreamBot.getInstance().joinAudio(channel);
        reply(ctx, "Joined audio channel `" + channel.getName() + "`.", Utils.colorGreen);
    }
}
