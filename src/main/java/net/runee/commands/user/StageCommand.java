package net.runee.commands.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;

import java.util.Locale;

public class StageCommand extends Command {
    public StageCommand() {
        super(new CommandData("stage", "Manage the bot users speech on stage"));
        data.addOption(OptionType.STRING, "op", "Operation to perform (start|end|speak|withdraw|topic|privacy)", true);
        data.addOption(OptionType.STRING, "new_value", "New stage topic or privacy level, only valid if op = 'topic' or 'privacy'", false);
        data.addOption(OptionType.BOOLEAN, "public", "Whether to show this command to others or not", false);
    }

    @Override
    public void run(SlashCommandEvent ctx) throws CommandException {
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

        VoiceChannel voiceChannel = guild.getAudioManager().getConnectedChannel();
        if (!(voiceChannel instanceof StageChannel)) {
            reply(ctx, "Not connected - join a stage channel first!", Utils.colorRed);
            return;
        }
        StageChannel channel = (StageChannel) voiceChannel;
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
                stage.requestToSpeak().queue(ignore -> {
                    reply(ctx, "Requested to speak on stage.", Utils.colorGreen);
                });
                break;
            case "withdraw":
                stage.cancelRequestToSpeak().queue(ignore -> {
                    reply(ctx, "Canceled request to speak on stage.", Utils.colorGreen);
                });
                break;
            case "topic":
                stage.getManager().setTopic(topicOrPrivacy);
                break;
            case "privacy":
                StageInstance.PrivacyLevel privacy;
                switch (topicOrPrivacy) {
                    case "guild":
                        privacy = StageInstance.PrivacyLevel.GUILD_ONLY;
                        break;
                    case "public":
                        privacy = StageInstance.PrivacyLevel.PUBLIC;
                        break;
                    default:
                        reply(ctx, "Unrecognized stage privacy level: `" + topicOrPrivacy + "`", Utils.colorRed);
                        return;
                }
                stage.getManager().setPrivacyLevel(privacy);
                break;
            default:
                reply(ctx, "Unrecognized operation: `" + op + "`.", Utils.colorRed);
                break;
        }

    }
}
