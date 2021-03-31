package net.runee.commands.settings;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.runee.errors.CommandException;
import net.runee.errors.IncorrectArgCountException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.misc.discord.CommandCategory;
import net.runee.misc.discord.CommandContext;
import net.runee.model.Config;
import net.runee.model.GuildConfig;

import java.util.List;

public class FollowVoiceCommand extends Command {
    public FollowVoiceCommand() {
        super("follow-voice", "Marks a user to be followed in along voice channels.", CommandCategory.SETTINGS);
        this.arguments.add(new Argument("op", "Operation to perform", "set|clear|show"));
        this.arguments.add(new Argument("user", "User in question. Only valid if op = 'set'", "User", true));
    }

    @Override
    public void execute(CommandContext ctx, String... args) throws CommandException {
        Guild guild = ctx.ensureAdminOrOwnerPermission();
        if(args.length >= 1) {
            String action = args[0];
            switch (action) {
                case "set":
                    if(args.length != 2) {
                        throw new IncorrectArgCountException(this, ctx);
                    }
                    Member target;
                    List<Member> targetMatches = Utils.findMember(guild, args[1]);
                    switch (targetMatches.size()) {
                        case 0:
                            ctx.replyWarning("Can't find specified user!");
                            return;
                        case 1:
                            target = targetMatches.get(0);
                            break;
                        default:
                            ctx.replyWarning("There are multiple users with that name/nick!");
                            return;
                    }
                    setFollowVoice(ctx, guild, target);
                    break;
                case "clear":
                    if(args.length != 1) {
                        throw new IncorrectArgCountException(this, ctx);
                    }
                    setFollowVoice(ctx, guild, null);
                    break;
                case "show":
                    if(args.length != 1) {
                        throw new IncorrectArgCountException(this, ctx);
                    }
                    showFollowVoice(ctx, guild);
                    break;
            }
        }
    }

    private void setFollowVoice(CommandContext ctx, Guild guild, Member target) {
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        guildConfig.followedUserId = target != null ? target.getId() : null;
        saveConfig();
        if (target != null) {
            ctx.replySuccess("Follow target updated.");
        } else {
            ctx.replySuccess("Follow target removed.");
        }
    }

    private void showFollowVoice(CommandContext ctx, Guild guild) {
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        if (guildConfig.commandPrefix != null) {
            ctx.replySuccess("Current follow target: " + formatMember(ctx.getJDA(), guildConfig.guildId, guildConfig.followedUserId) + ".");
        } else {
            ctx.replySuccess("No follow target is currently set.");
        }
    }

    private String formatMember(JDA jda, String guildId, String userId) {
        Guild guild = jda.getGuildById(guildId);
        Member member = guild != null ? guild.getMemberById(userId) : null;
        return "`" + (member != null ? Utils.formatUser(member.getUser()) : userId) + "`";
    }
}
