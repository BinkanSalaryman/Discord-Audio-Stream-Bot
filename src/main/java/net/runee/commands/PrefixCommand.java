package net.runee.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.io.IOException;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        this.name = "prefix";
        this.arguments = "action:set|clear|show [prefix]";
        this.summary = "Get or set current servers' command prefix.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        if (args.length == 0) {
            throw new IncorrectArgCountException(this, ctx);
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "set":
                if (args.length != 2) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                break;
            case "clear":
            case "show":
                if (args.length != 1) {
                    throw new IncorrectArgCountException(this, ctx);
                }
                break;
            default:
                ctx.replyWarning("Unrecognized action: `" + action + "`.");
                break;
        }

        // execute
        switch (action) {
            case "set":
                setPrefix(ctx, args[1]);
                break;
            case "clear":
                setPrefix(ctx, null);
                break;
            case "show":
                showPrefix(ctx);
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    private void showPrefix(CommandContext ctx) throws CommandException {
        final Guild guild = ctx.ensureGuildContext();
        GuildConfig guildConfig = getConfig().getGuildConfig(guild);
        if (guildConfig != null && guildConfig.commandPrefix != null) {
            ctx.replySuccess("Current command prefix: `" + guildConfig.commandPrefix + "`.");
        } else {
            ctx.replySuccess("No command prefix is currently set.");
        }
    }

    private void setPrefix(CommandContext ctx, String prefix) throws CommandException {
        final Guild guild = ctx.ensureAdminPermission();
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        if (guildConfig != null) {
            guildConfig.commandPrefix = prefix;
        } else {
            guildConfig = new GuildConfig(guild);
            guildConfig.commandPrefix = prefix;
            config.addGuildConfig(guildConfig);
        }
        saveConfig();
        if (prefix != null) {
            ctx.replySuccess("Command prefix updated.");
        } else {
            ctx.replySuccess("Command prefix removed.");
        }
    }
}
