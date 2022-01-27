package at.xirado.bean.command;

import at.xirado.bean.Bean;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.LinkedDataObject;
import at.xirado.bean.misc.Util;
import at.xirado.bean.translation.LocaleLoader;
import lavalink.client.io.LavalinkSocket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import javax.annotation.CheckReturnValue;
import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

public class SlashCommandContext {

    public static final String DENY = "\uD83D\uDEAB";
    public static final String ERROR = "❌";
    public static final String SUCCESS = "✅";
    public String language;

    private final SlashCommandEvent event;

    public SlashCommandContext(SlashCommandEvent event) {
        this.event = event;
        if (event.getGuild() != null) {
            Locale serverLocale = event.getGuild().getLocale();
            if (LocaleLoader.getForLanguage(serverLocale.toLanguageTag()) == null) {
                this.language = "en_US";
            } else {
                this.language = serverLocale.toLanguageTag();
            }
        } else {
            language = "en_US";
        }
    }

    public boolean isLavalinkNodeAvailable() {
        boolean available = false;
        for (LavalinkSocket sock : Bean.getInstance().getLavalink().getNodes()) {
            if (sock.isAvailable())
                return true;
        }
        return false;
    }

    public LavalinkSocket getAvailableNode() {
        for (LavalinkSocket sock : Bean.getInstance().getLavalink().getNodes()) {
            if (sock.isAvailable())
                return sock;
        }
        return null;
    }

    public GuildData getGuildData() {
        return this.event.getGuild() == null ? null : GuildManager.getGuildData(this.event.getGuild());
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

    public ReplyAction reply(String content) {
        return event.reply(content).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public ReplyAction reply(Message message) {
        return event.reply(message).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    @CheckReturnValue
    public ReplyAction reply(MessageEmbed embed, MessageEmbed... embeds) {
        return event.replyEmbeds(embed, embeds).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public ReplyAction replyFormat(String format, Object... args) {
        return event.replyFormat(format, args).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    @CheckReturnValue
    public ReplyAction replyError(String content) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(ERROR + " " + content);

        return event.replyEmbeds(builder.build()).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public ReplyAction replyErrorFormat(String format, Object... args) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(ERROR + " " + String.format(format, args));

        return event.replyEmbeds(builder.build()).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public String parseDuration(long seconds, String delimiter) {
        return LocaleLoader.parseDuration(seconds, getLanguage(), delimiter);
    }

}
