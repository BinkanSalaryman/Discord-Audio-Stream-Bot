package net.runee.commands.settings;

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
        super("autojoin", "Marks a voice channel to be connected to after login.", CommandCategory.SETTINGS);
        this.arguments.add(new Argument("op", "Operation to perform", "set|clear|show"));
        this.arguments.add(new Argument("channel", "Voice channel in question. Only valid if op = 'set'", "VoiceChannel", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureAdminOrOwnerPermission();

        // parse args
        if (args.length < 1) {
            throw new IncorrectArgCountException(this, ctx);
        }

        String op = args[0].toLowerCase();
        VoiceChannel channel = null;

        switch (op) {
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
                ctx.replyWarning("Unrecognized operation: `" + op + "`.");
                return;
        }

        // execute
        GuildConfig guildConfig = getConfig().getGuildConfig(ctx.getGuild());

        switch (op) {
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
            ctx.replySuccess("Current auto-join voice channel: " + formatChannel(ctx, guildConfig.autoJoinVoiceChanncelId) + ".");
        } else {
            ctx.replySuccess("No auto-join voice channel is currently set.");
        }
    }

    private String formatChannel(CommandContext ctx, String channelId) {
        VoiceChannel voiceChannel = ctx.getJDA().getVoiceChannelById(channelId);
        return "`" + (voiceChannel != null ? Utils.formatChannel(voiceChannel) : channelId) + "`";
    }
}
