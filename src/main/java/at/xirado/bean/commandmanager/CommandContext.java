package at.xirado.bean.commandmanager;

import at.xirado.bean.translation.I18n;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction;

import java.util.Arrays;
import java.util.Locale;

public class CommandContext
{

    public static final String DENY = "\uD83D\uDEAB";
    public static final String ERROR = "❌";
    public static final String SUCCESS = "✅";
    public String language;

    private final SlashCommandEvent event;

    public CommandContext(SlashCommandEvent event)
    {
        this.event = event;
        if(event.getGuild() != null)
        {
            Locale serverLocale = event.getGuild().getLocale();
            if(I18n.getForLanguage(serverLocale.toLanguageTag()) == null)
            {
                this.language = "en_US";
            }else
            {
                this.language = serverLocale.toLanguageTag();
            }
        }else {
            language = "en_US";
        }


    }


    public String getLocalized(String query, Object... objects)
    {
        Guild g = event.getGuild();
        if(g != null) return String.format(I18n.ofGuild(g).get(query), objects);
        return String.format(I18n.getForLanguage("en_US").get(query), objects);
    }

    public I18n getLanguage()
    {
        Guild g = event.getGuild();
        I18n language;
        if(g != null) language =  I18n.ofGuild(g);
        else language = I18n.getForLanguage("en_US");
        return language;
    }

    public CommandReplyAction reply(String content)
    {
        return event.reply(content).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public CommandReplyAction reply(Message message)
    {
        return event.reply(message).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public CommandReplyAction reply(MessageEmbed embed, MessageEmbed... embeds)
    {
        return event.reply(embed, embeds).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public CommandReplyAction replyFormat(String format, Object... args)
    {
        return event.replyFormat(format, args).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public CommandReplyAction replyError(String content)
    {
        return event.reply(ERROR+" "+content).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }

    public CommandReplyAction replyErrorFormat(String format, Object... args)
    {
        return event.replyFormat(ERROR+" "+format, args).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
    }


}
