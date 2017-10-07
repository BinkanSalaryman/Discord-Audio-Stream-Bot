using Discord.Commands;
using System.Reflection;

namespace DASB {
    public interface IBotAgent {
        string Id { get; }
        string Say(BotString @string);
        string Say(BotString @string, params object[] args);
        string Help(CommandAttribute command, int parameterCount);
    }

    public enum BotString {
        error_exception,
        error_unknownCommand,
        error_badArgCount,
        warning_unknownCommand,
        warning_notInGuildSelf,
        warning_notInGuildAuthor,
        warning_noPermissionConnect_specifiedChannel,
        warning_noPermissionConnect_authorsChannel,
        warning_badVoiceChannelId,
        warning_badUserId,
        warning_notInVoice,
        warning_voiceChannelNameAmbigous,
        warning_voiceChannelNameNotFound,
        info_pingSelf,
        info_pingSuccess,
        info_pingFailed,
        info_permissionsHeadSelf,
        info_permissionsHeadOther,
        info_mailHead,
    }
}
