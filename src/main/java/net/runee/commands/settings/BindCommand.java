package net.runee.commands.settings;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.util.List;
import java.util.stream.Collectors;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Manages which channels to accept commands from.", CommandCategory.SETTINGS);
        this.arguments.add(new Argument("op", "Operation to perform", "add|remove|clear|show"));
        this.arguments.add(new Argument("channel", "Channel to add/remove to list of accepted command channels. Only valid if op = 'add' or 'remove'", "TextChannel", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        final Guild guild = ctx.ensureGuildContext();

        // parse args
        if (args.length == 0) {
            throw new IncorrectArgCountException(this, ctx);
        }
        String action = args[0].toLowerCase();
        TextChannel channel;

        // verify
        switch (action) {
            case "add":
            case "remove":
            case "clear":
                ctx.ensureAdminOrOwnerPermission();
                break;
        }

        // parse args
        switch (action) {
            case "add":
            case "remove": {
                if (args.length != 2) {
                    throw new IncorrectArgCountException(this, ctx);
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
            case "clear":
            case "show":
                if (args.length != 1) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                channel = null;
                break;
            default:
                ctx.replyWarning("Unrecognized action: `" + action + "`.");
                return;
        }

        // execute
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        switch (action) {
            case "add":
                addCommandChannel(ctx, guildConfig, channel);
                break;
            case "remove":
                removeCommandChannel(ctx, guildConfig, channel);
                break;
            case "clear":
                clearCommandChannels(ctx, guildConfig);
                break;
            case "show":
                showCommandChannels(ctx, guildConfig);
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    private void addCommandChannel(CommandContext ctx, GuildConfig guildConfig, TextChannel channel) {
        int oldSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        guildConfig.addCommandChannel(channel);
        int newSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        if (newSize != oldSize) {
            saveConfig();
            ctx.replySuccess("Bindings updated.");
        } else {
            ctx.replyWarning("Channel is already bound.");
        }
    }

    private void removeCommandChannel(CommandContext ctx, GuildConfig guildConfig, TextChannel channel) {
        int oldSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        guildConfig.removeCommandChannel(channel);
        int newSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        if (newSize != oldSize) {
            saveConfig();
            ctx.replySuccess("Bindings updated.");
        }
        if (oldSize >= 0 && newSize < 0) {
            ctx.replyWarning("Last channel has been unbound - commands from any channel will be accepted.");
        }
    }

    private void clearCommandChannels(CommandContext ctx, GuildConfig guildConfig) {
        guildConfig.commandChannelIds = null;
        saveConfig();
        ctx.replySuccess("All bindings cleared - commands from any channel will be accepted.");
    }

    private void showCommandChannels(CommandContext ctx, GuildConfig guildConfig) {
        if (guildConfig.commandChannelIds != null) {
            if (guildConfig.commandChannelIds.size() == 1) {
                String channelId = guildConfig.commandChannelIds.iterator().next();
                ctx.replySuccess("Current command channel: " + formatChannel(ctx, channelId));
            } else {
                String commandChannelNamesStr = guildConfig.commandChannelIds
                        .stream()
                        .map(channelId -> "`" + formatChannel(ctx, channelId))
                        .collect(Collectors.joining("\n" + Utils.ucListItem));
                ctx.replySuccess("Current command channels:\n" + Utils.ucListItem + commandChannelNamesStr);
            }
        } else {
            ctx.replySuccess("No bindings set - commands from any channel will be accepted.");
        }
    }

    private String formatChannel(CommandContext ctx, String channelId) {
        TextChannel channel = ctx.getJDA().getTextChannelById(channelId);
        return "`" + (channel != null ? Utils.formatChannel(channel) : channelId) + "`";
    }
}
