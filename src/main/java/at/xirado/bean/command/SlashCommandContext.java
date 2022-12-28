package at.xirado.bean.command;

import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.CheckReturnValue;
import java.awt.*;
import java.util.Arrays;

public class SlashCommandContext {

    public static final String DENY = "\uD83D\uDEAB";
    public static final String ERROR = "❌";
    public static final String SUCCESS = "✅";

    private final GenericCommandInteractionEvent event;

    public SlashCommandContext(GenericCommandInteractionEvent event) {
        this.event = event;
    }

    public GuildData getGuildData() {
        return this.event.getGuild() == null ? null : GuildManager.getGuildData(this.event.getGuild());
    }

    public void sendSimpleEmbed(CharSequence content) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x452350)
                .setDescription(content);
        event.replyEmbeds(builder.build()).queue();
    }

    public void sendSimpleEphemeralEmbed(CharSequence content) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x452350)
                .setDescription(content);
        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    @CheckReturnValue
    public ReplyCallbackAction reply(MessageEmbed embed, MessageEmbed... embeds) {
        return event.replyEmbeds(embed, embeds).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    @CheckReturnValue
    public ReplyCallbackAction replyError(String content) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(ERROR + " " + content);

        return event.replyEmbeds(builder.build()).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }
}
