package net.runee.commands.settings;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.util.Locale;
import java.util.stream.Collectors;

public class BindCommand extends Command {
    public BindCommand() {
        super(new CommandData("bind", "Manage which channels to accept commands from"));
        data.addOption(OptionType.STRING, "op", "Operation to perform (add|remove|clear|show)", true);
        data.addOption(OptionType.CHANNEL, "channel", "Channel to add/remove to list of accepted command channels. Only valid if op = 'add' or 'remove'", false);
        _public = true;
    }

    @Override
    public void run(SlashCommandEvent ctx) throws CommandException {
        String op = ensureOptionPresent(ctx, "op").getAsString().toLowerCase(Locale.ROOT);

        final Guild guild = ensureGuildContext(ctx);
        switch (op) {
            case "add":
            case "remove":
            case "clear":
                ensureAdminOrOwnerPermission(ctx);
                break;
        }

        TextChannel channel;
        switch (op) {
            case "add":
            case "remove": {
                MessageChannel messageChannel = ensureOptionPresent(ctx, "channel").getAsMessageChannel();
                if (messageChannel instanceof TextChannel) {
                    channel = (TextChannel) messageChannel;
                } else {
                    reply(ctx, "Channel is not a from a guild", Utils.colorRed);
                    return;
                }
                break;
            }
            case "clear":
            case "show":
                ensureOptionAbsent(ctx, "channel");
                channel = null;
                break;
            default:
                reply(ctx, "Unrecognized operation: `" + op + "`.", Utils.colorRed);
                return;
        }

        // execute
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        switch (op) {
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

    private void addCommandChannel(SlashCommandEvent ctx, GuildConfig guildConfig, TextChannel channel) {
        int oldSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        guildConfig.addCommandChannel(channel);
        int newSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        if (newSize != oldSize) {
            saveConfig();
            reply(ctx, "Bindings updated.", Utils.colorGreen);
        } else {
            reply(ctx, "Channel is already bound.", Utils.colorRed);
        }
    }

    private void removeCommandChannel(SlashCommandEvent ctx, GuildConfig guildConfig, TextChannel channel) {
        int oldSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        guildConfig.removeCommandChannel(channel);
        int newSize = guildConfig.commandChannelIds != null ? guildConfig.commandChannelIds.size() : -1;
        if (newSize != oldSize) {
            saveConfig();
            reply(ctx, "Bindings updated.", Utils.colorGreen);
        }
        if (oldSize >= 0 && newSize < 0) {
            reply(ctx, "Last channel has been unbound - commands from any channel will be accepted.", Utils.colorRed);
        }
    }

    private void clearCommandChannels(SlashCommandEvent ctx, GuildConfig guildConfig) {
        guildConfig.commandChannelIds = null;
        saveConfig();
        reply(ctx, "All bindings cleared - commands from any channel will be accepted.", Utils.colorGreen);
    }

    private void showCommandChannels(SlashCommandEvent ctx, GuildConfig guildConfig) {
        if (guildConfig.commandChannelIds != null) {
            if (guildConfig.commandChannelIds.size() == 1) {
                String channelId = guildConfig.commandChannelIds.iterator().next();
                reply(ctx, "Current command channel: " + formatChannel(ctx, channelId), Utils.colorGreen);
            } else {
                String commandChannelNamesStr = guildConfig.commandChannelIds
                        .stream()
                        .map(channelId -> "`" + formatChannel(ctx, channelId))
                        .collect(Collectors.joining("\n" + Utils.ucListItem));
                reply(ctx, "Current command channels:\n" + Utils.ucListItem + commandChannelNamesStr, Utils.colorGreen);
            }
        } else {
            reply(ctx, "No bindings set - commands from any channel will be accepted.", Utils.colorGreen);
        }
    }

    private String formatChannel(SlashCommandEvent ctx, String channelId) {
        TextChannel channel = ctx.getJDA().getTextChannelById(channelId);
        return "`" + (channel != null ? Utils.formatChannel(channel) : channelId) + "`";
    }
}
