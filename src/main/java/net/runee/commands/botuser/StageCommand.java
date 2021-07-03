package net.runee.commands.botuser;

import net.dv8tion.jda.api.entities.*;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;

import java.util.List;

public class StageCommand extends Command {
    public StageCommand() {
        super("stage", "Requests to speak in the current stage channel.", CommandCategory.BOT_USER);
        this.arguments.add(new Argument("op", "Operation to perform", "start|end|speak|withdraw|topic|privacy"));
        this.arguments.add(new Argument("new_value", "New stage topic or privacy level, only valid if op = 'topic' or 'privacy'", "Text / guild|public", true));
        this.arguments.add(new Argument("guild", "Guild instance in question", "Guild", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        // parse args
        if (args.length < 1 || args.length > 3) {
            throw new IncorrectArgCountException(this, ctx);
        }

        String op = args[0];
        String topicOrPrivacy = args.length > 1 ? args[1] : null;

        Guild guild;
        if(args.length > 2) {
            // specific guild
            String guildSearch = args[2];
            List<Guild> guildMatches = Utils.findGuild(ctx.getJDA(), guildSearch);
            switch (guildMatches.size()) {
                case 0:
                    ctx.replyWarning("No such guild!");
                    return;
                case 1: {
                    guild = guildMatches.get(0);
                    Member authorAsMember = guild.getMember(ctx.getAuthor());
                    if (authorAsMember == null) {
                        ctx.ensureOwnerPermission();
                    } else {
                        guild = ctx.ensureAdminOrOwnerPermission();
                    }
                    break;
                }
                default:
                    ctx.replyWarning("There are multiple guilds with that name!");
                    return;
            }
        } else {
            // current guild
            guild = ctx.ensureAdminOrOwnerPermission();
        }

        // execute
        VoiceChannel voiceChannel = guild.getAudioManager().getConnectedChannel();
        if (!(voiceChannel instanceof StageChannel)) {
            ctx.replyWarning("Not connected - join a stage channel first!");
            return;
        }
        StageChannel channel = (StageChannel) voiceChannel;

        boolean stageOptional = false;
        boolean requireTopicOrPrivacy = false;

        switch (op) {
            case "start":
                stageOptional = true;
                requireTopicOrPrivacy = true;
                break;
            case "topic":
            case "privacy":
                requireTopicOrPrivacy = true;
                break;
        }

        StageInstance stage = channel.getStageInstance();
        if (stage == null && !stageOptional) {
            ctx.replyWarning("No stage - start a stage first!");
            return;
        }
        if (requireTopicOrPrivacy && topicOrPrivacy == null) {
            ctx.replyWarning("Operation requires a stage topic or privacy level, please specify one.");
            return;
        }

        switch (op) {
            case "start":
                channel.createStageInstance(topicOrPrivacy).complete();
                ctx.replySuccess("Stage started.");
                break;
            case "end":
                stage.delete().queue(ignore -> {
                    ctx.replySuccess("Stage deleted.");
                });
                break;
            case "speak":
                stage.requestToSpeak().queue(ignore -> {
                    ctx.replySuccess("Requested to speak on stage.");
                });
                break;
            case "withdraw":
                stage.cancelRequestToSpeak().queue(ignore -> {
                    ctx.replySuccess("Canceled request to speak on stage.");
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
                        ctx.replyWarning("Unrecognized stage privacy level: `" + topicOrPrivacy + "`");
                        return;
                }
                stage.getManager().setPrivacyLevel(privacy);
                break;
            default:
                ctx.replyWarning("Unrecognized operation: `" + op + "`.");
                break;
        }

    }
}
