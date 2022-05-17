package at.xirado.bean.misc.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class CachedMessage {
    private final long messageId;
    private final long channelId;
    private final long authorId;
    private final String content;
    private final OffsetDateTime creationDate;
    private final JDA jda;

    public CachedMessage(@Nonnull Message message) {
        Checks.notNull(message, "Message");
        messageId = message.getIdLong();
        channelId = message.getChannel().getIdLong();
        authorId = message.getAuthor().getIdLong();
        content = message.getContentRaw();
        creationDate = message.getTimeCreated();
        jda = message.getJDA();
    }

    @Nonnull
    public RestAction<Message> retrieveMessage() {
        TextChannel channel = getChannel();
        Checks.check(channel != null, "Channel no longer exists!");
        return channel.retrieveMessageById(messageId);
    }

    @Nonnull
    public RestAction<Void> delete() {
        if (getChannel() == null)
            return new CompletedRestAction<>(jda, null);

        return getChannel().deleteMessageById(messageId);
    }

    @Nullable
    public TextChannel getChannel() {
        return jda.getTextChannelById(channelId);
    }

    public long getMessageId() {
        return messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }
}
