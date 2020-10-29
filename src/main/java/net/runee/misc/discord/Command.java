package net.runee.misc.discord;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.io.IOException;

public abstract class Command {
    protected String name;
    protected String summary = "";
    protected String arguments = "";
    protected CommandCategory category = CommandCategory.GENERAL;

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getArguments() {
        return arguments;
    }

    public CommandCategory getCategory() {
        return category;
    }

    public abstract void execute(CommandContext ctx, String... args) throws CommandException;

    protected Config getConfig() {
        return DiscordAudioStreamBot.getConfig();
    }

    protected void saveConfig() {
        try {
            DiscordAudioStreamBot.saveConfig();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
