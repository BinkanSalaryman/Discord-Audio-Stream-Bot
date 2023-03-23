package net.runee.commands.settings;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.runee.errors.CommandException;
import net.runee.misc.Utils;
import net.runee.misc.discord.Command;
import net.runee.model.Config;
import net.runee.model.GuildConfig;
import java.util.Locale;

public class FollowAudioCommand extends Command {
    public FollowAudioCommand() {
        super(Commands.slash("follow-audio", "Mark a user to be followed in along voice channels"));
        data.addOption(OptionType.STRING, "op", "Operation to perform (set|clear|show)", true);
        data.addOption(OptionType.USER, "user", "User in question. Only valid if op = 'set'", false);
        _public = true;
    }

    @Override
    public void run(SlashCommandInteractionEvent ctx) throws CommandException {
        Guild guild = ensureAdminOrOwnerPermission(ctx);

        String op = ensureOptionPresent(ctx, "op").getAsString().toLowerCase(Locale.ROOT);
        switch (op) {
            case "set": {
                Member target = ensureOptionPresent(ctx, "user").getAsMember();
                setFollowVoice(ctx, guild, target);
                break;
            }
            case "clear":
                ensureOptionAbsent(ctx, "user");
                setFollowVoice(ctx, guild, null);
                break;
            case "show":
                ensureOptionAbsent(ctx, "user");
                showFollowVoice(ctx, guild);
                break;
            default:
                reply(ctx, "Unrecognized operation: `" + op + "`.", Utils.colorRed);
                break;
        }
    }

    private void setFollowVoice(SlashCommandInteractionEvent ctx, Guild guild, Member target) {
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        guildConfig.followedUserId = target != null ? target.getId() : null;
        saveConfig();
        if (target != null) {
            reply(ctx, "Follow target updated.", Utils.colorGreen);
        } else {
            reply(ctx, "Follow target removed.", Utils.colorGreen);
        }
    }

    private void showFollowVoice(SlashCommandInteractionEvent ctx, Guild guild) {
        final Config config = getConfig();
        GuildConfig guildConfig = config.getGuildConfig(guild);
        if (guildConfig.followedUserId != null) {
            reply(ctx, "Current follow target: " + formatMember(ctx.getJDA(), guildConfig.guildId, guildConfig.followedUserId) + ".", Utils.colorGreen);
        } else {
            reply(ctx, "No follow target is currently set.", Utils.colorGreen);
        }
    }

    private String formatMember(JDA jda, String guildId, String userId) {
        Guild guild = jda.getGuildById(guildId);
        Member member = guild != null ? guild.getMemberById(userId) : null;
        return "`" + (member != null ? Utils.formatUser(member.getUser()) : userId) + "`";
    }
}
