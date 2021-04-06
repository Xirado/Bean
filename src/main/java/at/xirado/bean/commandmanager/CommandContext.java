package at.xirado.bean.commandmanager;

import at.xirado.bean.translation.TranslationHandler;
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
            if(TranslationHandler.getForLanguage(serverLocale.toLanguageTag()) == null)
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

    public String getTranslated(String qry)
    {
        return TranslationHandler.getForLanguage(this.language).get(qry);
    }

    public CommandReplyAction localizedReply(String qry, Object... objects)
    {
        return event.replyFormat(TranslationHandler.getForLanguage(this.language).get(qry), objects).allowedMentions(Arrays.asList(Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER));
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


}
