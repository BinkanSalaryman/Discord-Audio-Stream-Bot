package net.runee.commands.user;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

import java.util.Locale;

public class StatusCommand extends Command {
    public StatusCommand() {
        super(new CommandData("status", "Manage the online status"));
        data.addOption(OptionType.STRING, "status", "The new bot user's online status (online|idle|dnd|inv)", true);
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        ensureOwnerPermission(ctx);

        String status = ensureOptionPresent(ctx, "status").getAsString().toLowerCase(Locale.ROOT);

        switch (status) {
            case "online":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
                break;
            case "idle":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
                break;
            case "dnd":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                break;
            case "inv":
                ctx.getJDA().getPresence().setStatus(OnlineStatus.INVISIBLE);
                break;
            default:
                reply(ctx, "Unrecognized status: `" + status + "`.", Utils.colorRed);
                return;
        }
        reply(ctx, "Status updated.", Utils.colorGreen);
    }
}
