package net.runee.commands.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class LeaveGuildCommand extends Command {
    public LeaveGuildCommand() {
        super(new CommandData("leave-guild", "Leave the guild"));
        _public = true;
    }

    @Override
    public void run(SlashCommandEvent ctx) throws CommandException {
        Guild guild = ensureAdminOrOwnerPermission(ctx);
        guild.leave().queue(ignore -> {
            reply(ctx, "Guild left.", Utils.colorGreen);
        });
    }
}
