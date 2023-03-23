package net.runee.commands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

public class InviteCommand extends Command {
    public InviteCommand() {
        super(Commands.slash("invite", "Show this bots invite link"));
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        ctx.replyEmbeds(new EmbedBuilder()
                        .setTitle("Authorize access to your account", DiscordAudioStreamBot.getInstance().getJDA().getInviteUrl(Permission.EMPTY_PERMISSIONS))
                        .setDescription("Wanna invite me to your guild?")
                        .setColor(Utils.colorYellow)
                        .build()
                )
                .setEphemeral(!_public)
                .queue()
        ;
    }
}
