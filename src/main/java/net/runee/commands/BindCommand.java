package net.runee.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BindCommand extends Command {
    public BindCommand() {
        this.name = "bind";
        this.arguments = "action:add|remove|clear|show [channel]";
        this.help = "Modifies the allowed command channel list.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        final Guild guild = ctx.ensureGuildContext();

        // parse args
        if (args.length == 0) {
            throw new IncorrectArgCountException(this, ctx);
        }
        String action = args[0];
        TextChannel channel;

        switch (action) {
            case "add":
            case "remove": {
                if (args.length != 2) {
                    ctx.replyWarning("Incorrect number of arguments!");
                    return;
                }
                String channelSearch = args[1];
                List<TextChannel> channelMatches = Utils.findTextChannel(guild, channelSearch);

                switch (channelMatches.size()) {
                    case 0:
                        ctx.replyWarning("No such text channel!");
                        return;
                    case 1:
                        channel = channelMatches.get(0);
                        break;
                    default:
                        ctx.replyWarning("There are multiple text channels with that name!");
                        return;
                }
                break;
            }
            default:
                if (args.length != 1) {
                    ctx.replyWarning("Incorrect number of arguments!");
                    return;
                }
                channel = null;
                break;
        }

        // execute
        final Config config = DiscordAudioStreamBot.getInstance().getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        if (guildConfig == null) {
            guildConfig = new GuildConfig(guild);
            config.addGuildConfig(guildConfig);
        }
        switch (action) {
            case "add":
                guildConfig.addCommandChannel(channel);
                break;
            case "remove":
                guildConfig.removeCommandChannel(channel);
                break;
            case "clear":
                guildConfig.commandChannelIds = null;
                break;
            case "show":
                showAllowedCommandChannels(ctx, guildConfig);
                return;
            default:
                ctx.replyWarning("Unrecognized list action provided!");
                return;
        }
        try {
            DiscordAudioStreamBot.getInstance().saveConfig();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        ctx.replySuccess("Allowed command channel list updated.");
    }

    private void showAllowedCommandChannels(CommandContext ctx, GuildConfig guildConfig) {
        if (guildConfig.commandChannelIds != null) {
            if (guildConfig.commandChannelIds.size() == 1) {
                String channelId = guildConfig.commandChannelIds.get(0);
                String commandChannelNameStr = formatTextChannelById(ctx.getJDA(), channelId);
                ctx.replySuccess("Current allowed command channel: " + commandChannelNameStr);
            } else {
                String commandChannelNamesStr = guildConfig.commandChannelIds
                        .stream()
                        .map(channelId -> formatTextChannelById(ctx.getJDA(), channelId))
                        .collect(Collectors.joining("\n- "));
                ctx.replySuccess("Current allowed command channels:\n- " + commandChannelNamesStr);
            }
        } else {
            ctx.replySuccess("No allowed command channels are currently set. Commands can be written in any text channel.");
        }
    }

    private String formatTextChannelById(JDA jda, String id) {
        TextChannel channel = jda.getTextChannelById(id);
        return channel != null ? "`" + channel.getName() + "`" : id;
    }
}
