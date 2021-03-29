package at.xirado.bean.commandmanager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction;

import java.util.Arrays;

public class CommandContext
{
    private final SlashCommandEvent event;

    public CommandContext(SlashCommandEvent event)
    {
        this.event = event;
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
