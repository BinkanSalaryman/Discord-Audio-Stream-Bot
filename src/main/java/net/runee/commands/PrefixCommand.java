package net.runee.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandContext;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.io.IOException;
import java.util.Arrays;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        this.name = "prefix";
        this.arguments = "[new_prefix|clear]";
        this.help = "Get or set current servers' command prefix.";
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        ctx.ensureGuildContext();
        Guild guild = ctx.getGuild();

        switch (args.length) {
            case 0:
                getPrefix(ctx, guild);
                break;
            case 1:
                String prefix = args[0];
                setPrefix(ctx, guild, prefix);
                break;
            default:
                throw new IncorrectArgCountException(this, ctx);
        }
    }

    private void getPrefix(CommandContext ctx, Guild guild) {
        GuildConfig guildConfig = DiscordAudioStreamBot.getInstance().getConfig().getGuildConfig(guild);
        if (guildConfig != null && guildConfig.commandPrefix != null) {
            ctx.replySuccess("Current command prefix is `" + guildConfig.commandPrefix + "`.");
        } else {
            ctx.replySuccess("No command prefix is currently set.");
        }
    }

    private void setPrefix(CommandContext ctx, Guild guild, String prefix) {
        if ("clear".equals(prefix)) {
            prefix = null;
        }
        final Config config = DiscordAudioStreamBot.getInstance().getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        if (guildConfig != null) {
            guildConfig.commandPrefix = prefix;
        } else {
            guildConfig = new GuildConfig(guild);
            guildConfig.commandPrefix = prefix;
            config.addGuildConfig(guildConfig);
        }
        try {
            DiscordAudioStreamBot.getInstance().saveConfig();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if(prefix != null) {
            ctx.replySuccess("Command prefix set to `" + prefix + "`.");
        } else {
            ctx.replySuccess("Command prefix removed.");
        }
    }
}
