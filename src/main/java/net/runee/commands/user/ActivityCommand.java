package net.runee.commands.user;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

import java.util.Locale;

public class ActivityCommand extends Command {
    public ActivityCommand() {
        super(Commands.slash("activity", "Manage the bot users activity"));
        ((SlashCommandData)data).addOption(OptionType.STRING, "type", "The type of the new bot users activity (playing|streaming|listening|watching|competing)", true);
        ((SlashCommandData)data).addOption(OptionType.STRING, "what", "Description of what the bot is doing", true);
        ((SlashCommandData)data).addOption(OptionType.STRING, "url", "A link to the aforementioned activity. Only valid if type = 'streaming'", false);
        ((SlashCommandData)data).addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        ensureOwnerPermission(ctx);

        String type = ensureOptionPresent(ctx, "type").getAsString().toLowerCase(Locale.ROOT);
        String what = ensureOptionPresent(ctx, "what").getAsString();
        String url = null;

        switch (type) {
            case "streaming": {
                OptionMapping option = ctx.getOption("url");
                if (option != null) {
                    url = option.getAsString();
                }
                break;
            }
            default:
                ensureOptionAbsent(ctx, "url");
                break;
        }

        // execute
        switch (type) {
            case "playing":
                ctx.getJDA().getPresence().setActivity(Activity.playing(what));
                break;
            case "streaming":
                if (!Activity.isValidStreamingUrl(url)) {
                    reply(ctx, "Invalid streaming url.", Utils.colorRed);
                    return;
                }
                ctx.getJDA().getPresence().setActivity(Activity.streaming(what, url));
                break;
            case "listening":
                ctx.getJDA().getPresence().setActivity(Activity.listening(what));
                break;
            case "watching":
                ctx.getJDA().getPresence().setActivity(Activity.watching(what));
                break;
            case "competing":
                ctx.getJDA().getPresence().setActivity(Activity.competing(what));
                break;
            default:
                reply(ctx, "Unrecognized activity type: `" + type + "`.", Utils.colorRed);
                return;
        }
        reply(ctx, "Activity updated.", Utils.colorGreen);
    }
}
