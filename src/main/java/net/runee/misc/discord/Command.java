package net.runee.misc.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.*;
import net.runee.model.Config;

import java.awt.*;
import java.io.IOException;
public abstract class Command {
    protected final CommandData data;
    protected boolean _public = false;

    protected Command(CommandData data) {
        this.data = data;
    }

    public CommandData getData() {
        return data;
    }

    public boolean isPublic() {
        return _public;
    }

    public abstract void run(SlashCommandInteractionEvent ctx) throws CommandException;

    protected void reply(SlashCommandInteractionEvent ctx, CharSequence text, Color color) {
        reply(ctx, text, color, _public);
    }

    protected void reply(SlashCommandInteractionEvent ctx, CharSequence text, Color color, boolean _public) {
        ctx.replyEmbeds(new EmbedBuilder()
                .setDescription(text)
                .setColor(color)
                .build()
        ).setEphemeral(!_public).queue();
    }

    protected Boolean getOptionalBoolean(SlashCommandInteractionEvent ctx, String optionName) {
        OptionMapping option = ctx.getOption(optionName);
        return option != null ? option.getAsBoolean() : null;
    }

    protected boolean getOptionalBoolean(SlashCommandInteractionEvent ctx, String optionName, boolean _default) {
        OptionMapping option = ctx.getOption(optionName);
        return option != null ? option.getAsBoolean() : _default;
    }

    protected boolean isGuildContext(SlashCommandInteractionEvent ctx) {
        return ctx.getGuild() != null;
    }

    protected Guild ensureGuildContext(SlashCommandInteractionEvent ctx) throws GuildContextRequiredException {
        if (ctx.getGuild() == null) {
            throw new GuildContextRequiredException(this, ctx);
        }
        return ctx.getGuild();
    }

    protected boolean isOwner(SlashCommandInteractionEvent ctx) {
        ApplicationInfo appInfo = ctx.getJDA().retrieveApplicationInfo().complete();
        return ctx.getUser().equals(appInfo.getOwner());
    }

    protected boolean isAdmin(SlashCommandInteractionEvent ctx) {
        if(ctx.getGuild() == null) {
            return false;
        }
        
        return ctx.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    protected Guild ensureAdminOrOwnerPermission(SlashCommandInteractionEvent ctx) throws InsufficientPermissionsException, GuildContextRequiredException {
        ensureGuildContext(ctx);
        if(!isAdmin(ctx)) {
            ensureOwnerPermission(ctx);
        }
        return ctx.getGuild();
    }

    protected void ensureOwnerPermission(SlashCommandInteractionEvent ctx) throws InsufficientPermissionsException {
        if(!isOwner(ctx)) {
            throw new InsufficientPermissionsException(this, ctx);
        }
    }

    protected OptionMapping ensureOptionPresent(SlashCommandInteractionEvent ctx, String optionName) throws MissingOptionException {
        OptionMapping option = ctx.getOption(optionName);
        if(option == null) {
            throw new MissingOptionException(this, ctx, optionName);
        }
        return option;
    }

    protected OptionMapping[] ensureOptionPresent(SlashCommandInteractionEvent ctx, String... optionNames) throws MissingOptionException {
        OptionMapping[] options = new OptionMapping[optionNames.length];
        for (int i = 0; i < options.length; i++) {
            options[i] = ensureOptionPresent(ctx, optionNames[i]);
        }
        return options;
    }

    protected void ensureOptionAbsent(SlashCommandInteractionEvent ctx, String optionName) throws IllegalOptionException {
        if(ctx.getOption(optionName) != null) {
            throw new IllegalOptionException(this, ctx, "channel");
        }
    }

    protected void ensureOptionAbsent(SlashCommandInteractionEvent ctx, String... optionNames) throws IllegalOptionException {
        for (String optionName : optionNames) {
            ensureOptionAbsent(ctx, optionName);
        }
    }
    
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
