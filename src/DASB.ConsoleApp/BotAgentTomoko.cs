using System;
using System.Reflection;
using Discord.Commands;
using Microsoft.Extensions.DependencyInjection;

namespace DASB {
    public class BotAgentTomoko : IBotAgent {
        public string Id => "tomoko";

        public string Say(BotString @string) {
            switch (@string) {
                case BotString.error_unknownCommand: return "Hmm I don't understand. Try asking for `help` first.";
                case BotString.error_badArgCount: return "Hmm I don't understand. Try adding or removing some details.";
                case BotString.warning_unknownCommand: return "**Unkown command:** *{0}*.";
                case BotString.warning_notInGuildSelf: return "I am not a member of that guild.";
                case BotString.warning_notInGuildAuthor: return "You are not a member of the guild.";
                case BotString.warning_noPermissionConnect_specifiedChannel: return "I am missing permissions to connect to that voice channel.";
                case BotString.warning_noPermissionConnect_authorsChannel: return "I am missing permissions to connect to your voice channel.";
                case BotString.warning_badVoiceChannelId: return "I can't find a voice channel with this id.";
                case BotString.warning_badUserId: return "I don't know this person.";
                case BotString.warning_notInVoice: return "I am not connected to a voice channel.";
                case BotString.warning_voiceChannelNameAmbigous: return "I see there are multiple voice channels with this name, tell me the channel id instead."; ;
                case BotString.warning_voiceChannelNameNotFound: return "I couldn't find that voice channel.";
                case BotString.info_pingSelf: return "Pong! ({0} ms)";
                case BotString.info_pingSuccess: return "It took {0} ms to get a reply.";
                case BotString.info_pingFailed: return "I couldn't get a reply, looks dead to me...";
                case BotString.info_permissionsHeadSelf: return "**Permissions:**";
                case BotString.info_permissionsHeadOther: return "**Permissions for {0}:**";
                case BotString.info_mailHead: return "**From {0}:**";
                default: return "Hmm something's fishy with tha. .  .   .    .     beep, boop.";
            }
        }

        public string Say(BotString @string, params object[] args) {
            return string.Format(Say(@string), args);
        }

        public string Help(CommandAttribute command, int parameterCount) {
            switch (command.Text) {
                case Commands.setStreaming: return "Changes what I am streaming";
                case Commands.setPlaying: return "Changes what I am playing";
                case Commands.setStatus: return "Changes my online status";
                case Commands.setNickname: return "Changes my nickname";
                case Commands.sendMail: return "Instructs me to forward a message";
                case Commands.showApplicationsLink: return "Shows the bot applications page";

                case Commands.showAuthorizeLink: return "Invites me to your server";
                case Commands.stop: return "Sends me to sleep";
                case Commands.joinVoice: return "Tells me to join the voice chat";
                case Commands.leaveVoice: return "Tells me to leave the voice chat";
                case Commands.help: return parameterCount == 0 ? "Makes me pointing you in the right direction": "Makes me tell you more about something";
                case Commands.listCommands: return "Makes me list what I can do";
                case Commands.assignPermissions: return "Tells me what someone can do with me (list separated by whitespaces): [command] grants, ![command] revokes, ~[command] clears, * grants all except revoked commands, !* revokes all except granted, ~* clears all";
                case Commands.listPermissions: return parameterCount == 0 ? "Makes me tell you your privileges" : "Makes me tell someone else's privileges";
                case Commands.ping: return "Pokes me or someone else";
                default: return null;
            }
        }
    }
}
