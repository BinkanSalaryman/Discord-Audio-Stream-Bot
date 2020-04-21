package net.runee.misc.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.runee.errors.CommandException;
import net.runee.errors.GuildContextRequiredException;
import net.runee.misc.Utils;

public class CommandContext {
    public CommandContext(JDA jda, User author, MessageChannel replyChannel) {
        this(jda, author, replyChannel, null);
    }

    public CommandContext(JDA jda, User author, MessageChannel replyChannel, Guild guild) {
        this.jda = jda;
        this.author = author;
        this.replyChannel = replyChannel;
        this.guild = guild;
    }

    public CommandContext(MessageReceivedEvent e) {
        this(e.getJDA(), e.getAuthor(), e.getChannel(), e.isFromGuild() ? e.getGuild() : null);
    }

    // core
    private JDA jda;
    private User author;
    private MessageChannel replyChannel;
    private Guild guild;
    // convenience
    private Command command;

    // core
    public JDA getJDA() {
        return jda;
    }

    public User getAuthor() {
        return author;
    }

    public MessageChannel getReplyChannel() {
        return replyChannel;
    }

    public void setReplyChannel(MessageChannel replyChannel) {
        this.replyChannel = replyChannel;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }

    public void run(Command command, String... args) {
        this.command = command;
        try {
            command.execute(this, args);
        } catch (CommandException ex) {
            reply(new EmbedBuilder()
                    .setDescription(ex.getReplyMessage())
                    .setColor(Utils.colorRed)
                    .build()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            replyWarning("Failed to execute command, please take a look at the console for details!");
        }
        this.command = null;
    }

    // convenience
    public void reply(MessageEmbed embed) {
        replyChannel.sendMessage(embed).queue();
    }

    public void reply(Message msg) {
        replyChannel.sendMessage(msg).queue();
    }

    public void reply(CharSequence text) {
        replyChannel.sendMessage(text).queue();
    }

    public void replyWarning(CharSequence text) {
        reply(new EmbedBuilder()
                .setDescription(text)
                .setColor(Utils.colorRed)
                .build()
        );
    }

    public void replySuccess(CharSequence text) {
        reply(new EmbedBuilder()
                .setDescription(text)
                .setColor(Utils.colorGreen)
                .build()
        );
    }

    public boolean isGuildContext() {
        return guild != null;
    }

    public Guild ensureGuildContext() throws GuildContextRequiredException {
        if (guild == null) {
            throw new GuildContextRequiredException(command, this);
        }
        return guild;
    }
}
