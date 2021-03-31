package net.runee.misc.discord;

import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.model.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Command {
    protected String name;
    protected String summary;
    protected List<Argument> arguments = new ArrayList<>();
    protected CommandCategory category;

    public Command(String name, String summary, CommandCategory category) {
        this.name = name;
        this.summary = summary;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
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

    public static class Argument {
        private String name;
        private String summary;
        private String type;
        private boolean isOptional;

        public Argument(String name, String summary, String type) {
            this(name, summary, type, false);
        }

        public Argument(String name, String summary, String type, boolean isOptional) {
            this.name = name;
            this.summary = summary;
            this.type = type;
            this.isOptional = isOptional;
        }

        public String getName() {
            return name;
        }

        public String getSummary() {
            return summary;
        }

        public String getType() {
            return type;
        }

        public boolean isEnum() {
            return type.contains("|");
        }

        public String[] getEnumValues() {
            return type.split("\\|");
        }

        public boolean isOptional() {
            return isOptional;
        }

        @Override
        public String toString() {
            return isOptional ? "[" + name + "]" : name;
        }
    }
}
