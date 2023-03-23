package net.runee.commands.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import java.util.Locale;

public class StageCommand extends Command {
    public StageCommand() {
        super(Commands.slash("stage", "Manage the bot users speech on stage"));
        data.addOption(OptionType.STRING, "op", "Operation to perform (start|end|speak|withdraw|topic)", true);
        data.addOption(OptionType.STRING, "new_value", "New stage topic, only valid if op = 'start' or 'topic'", false);
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        _public = getOptionalBoolean(ctx, "public", false);

        String op = ensureOptionPresent(ctx, "op").getAsString().toLowerCase(Locale.ROOT);
        String topicOrPrivacy = null;

        Guild guild = ensureAdminOrOwnerPermission(ctx);

        switch (op) {
            case "start":
            case "topic":
            case "privacy":
                topicOrPrivacy = ensureOptionPresent(ctx, "new_value").getAsString();
                break;
            default:
                ensureOptionAbsent(ctx, "new_value");
                break;
        }

        AudioChannelUnion audioChannel = guild.getAudioManager().getConnectedChannel();
        if (!(audioChannel instanceof StageChannel)) {
            reply(ctx, "Not connected - join a stage channel first!", Utils.colorRed);
            return;
        }
        StageChannel channel = audioChannel.asStageChannel();
        StageInstance stage = channel.getStageInstance();

        switch (op) {
            case "start":
                channel.createStageInstance(topicOrPrivacy).complete();
                reply(ctx, "Stage started.", Utils.colorGreen);
                return;
            default:
                if (stage == null) {
                    reply(ctx,"No stage - start a stage first!", Utils.colorRed);
                    return;
                }
                break;
        }

        switch (op) {
            case "end":
                stage.delete().queue(ignore -> {
                    reply(ctx, "Stage deleted.", Utils.colorGreen);
                });
                break;
            case "speak":
                channel.requestToSpeak().queue(ignore -> {
                    reply(ctx, "Requested to speak on stage.", Utils.colorGreen);
                });
                break;
            case "withdraw":
                channel.cancelRequestToSpeak().queue(ignore -> {
                    reply(ctx, "Canceled request to speak on stage.", Utils.colorGreen);
                });
                break;
            case "topic":
                stage.getManager().setTopic(topicOrPrivacy);
                break;
            default:
                reply(ctx, "Unrecognized operation: `" + op + "`.", Utils.colorRed);
                break;
        }

    }
}
