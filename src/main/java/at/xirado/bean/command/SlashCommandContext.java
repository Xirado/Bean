package at.xirado.bean.command;

import at.xirado.bean.Bean;
import at.xirado.bean.data.LinkedDataObject;
import at.xirado.bean.data.database.entity.DiscordGuild;
import at.xirado.bean.misc.Util;
import at.xirado.bean.translation.LocaleLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import javax.annotation.CheckReturnValue;
import java.awt.*;
import java.util.Arrays;

public class SlashCommandContext {

    public static final String DENY = "\uD83D\uDEAB";
    public static final String ERROR = "❌";
    public static final String SUCCESS = "✅";
    public String language;

    private final GenericCommandInteractionEvent event;

    public SlashCommandContext(GenericCommandInteractionEvent event) {
        this.event = event;
        if (event.getGuild() != null) {
            DiscordLocale serverLocale = event.getGuild().getLocale();
            if (LocaleLoader.getForLanguage(serverLocale.getLocale()) == null) {
                this.language = "en_US";
            } else {
                this.language = serverLocale.getLocale();
            }
        } else {
            language = "en_US";
        }
    }

    public DiscordGuild getGuildData() {
        return this.event.getGuild() == null
                ? null
                : Bean.getInstance().getRepository()
                  .getGuildRepository()
                  .getGuildDataBlocking(event.getGuild().getIdLong());
    }

    public String getLocalized(String query, Object... objects) {
        Guild g = event.getGuild();
        if (g != null) {
            String result = Util.format(LocaleLoader.ofGuild(g).get(query, String.class), objects);
            if (result != null)
                return result;
        }
        return Util.format(LocaleLoader.getForLanguage("en_US").get(query, String.class), objects);
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

    public MessageEmbed getSimpleEmbed(CharSequence content) {
        return new EmbedBuilder()
                .setColor(0x452350)
                .setDescription(content)
                .build();
    }

    public LinkedDataObject getLanguage() {
        Guild g = event.getGuild();
        LinkedDataObject language;
        if (g != null) language = LocaleLoader.ofGuild(g);
        else language = LocaleLoader.getForLanguage("en_US");
        return language;
    }

    public ReplyCallbackAction reply(String content) {
        return event.reply(content).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    public ReplyCallbackAction reply(Message message) {
        return event.reply(MessageCreateData.fromMessage(message)).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    @CheckReturnValue
    public ReplyCallbackAction reply(MessageEmbed embed, MessageEmbed... embeds) {
        return event.replyEmbeds(embed, embeds).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    public ReplyCallbackAction replyFormat(String format, Object... args) {
        return event.replyFormat(format, args).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    @CheckReturnValue
    public ReplyCallbackAction replyError(String content) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(ERROR + " " + content);

        return event.replyEmbeds(builder.build()).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    public ReplyCallbackAction replyErrorFormat(String format, Object... args) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(ERROR + " " + String.format(format, args));

        return event.replyEmbeds(builder.build()).setAllowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOJI, Message.MentionType.USER));
    }

    public String parseDuration(long seconds, String delimiter) {
        return LocaleLoader.parseDuration(seconds, getLanguage(), delimiter);
    }

}
