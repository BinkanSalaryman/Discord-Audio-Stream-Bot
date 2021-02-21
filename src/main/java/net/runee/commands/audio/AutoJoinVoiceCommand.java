package net.runee.commands.audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;
import net.runee.model.GuildConfig;

import java.util.List;

public class AutoJoinVoiceCommand extends Command {
    public AutoJoinVoiceCommand() {
        this.name = "autojoin";
        this.arguments = "action:set|clear|show [channel]";
        this.summary = "Marks a voice channel to be connected to after login.";
        this.category = CommandCategory.AUDIO;
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureAdminOrOwnerPermission();

        // parse args
        if (args.length < 1) {
            throw new IncorrectArgCountException(this, ctx);
        }

        String action = args[0].toLowerCase();
        VoiceChannel channel = null;

        switch (action) {
            case "set": {
                if (args.length != 2) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                String channelSearch = args[1];
                List<VoiceChannel> channelMatches = Utils.findVoiceChannel(ctx.getGuild(), channelSearch);
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
            case "clear":
            case "show":
                if (args.length != 1) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                break;
            default:
                ctx.replyWarning("Unrecognized action: `" + action + "`.");
                return;
        }

        // execute
        GuildConfig guildConfig = getConfig().getGuildConfig(ctx.getGuild());

        switch (action) {
            case "set":
            case "clear":
                setAutoVoiceChannel(ctx, guildConfig, channel);
                break;
            case "show":
                showAutoVoiceChannel(ctx, guildConfig);
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    private void setAutoVoiceChannel(CommandContext ctx, GuildConfig guildConfig, VoiceChannel channel) {
        guildConfig.autoJoinVoiceChanncelId = channel != null ? channel.getId() : null;
        saveConfig();
        if (channel != null) {
            ctx.replySuccess("Auto-join voice channel updated.");
        } else {
            ctx.replySuccess("Auto-join voice channel removed.");
        }
    }

    private void showAutoVoiceChannel(CommandContext ctx, GuildConfig guildConfig) {
        if (guildConfig.autoJoinVoiceChanncelId != null) {
            ctx.replySuccess("Current auto-join voice channel: `" + formatVoiceChannelById(ctx.getJDA(), guildConfig.autoJoinVoiceChanncelId) + "`.");
        } else {
            ctx.replySuccess("No auto-join voice channel is currently set.");
        }
    }

    private String formatVoiceChannelById(JDA jda, String id) {
        VoiceChannel channel = jda.getVoiceChannelById(id);
        return channel != null ? channel.getName() : id;
    }
}
