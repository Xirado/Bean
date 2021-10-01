package at.xirado.bean.interactions;

import at.xirado.bean.misc.EmbedUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ButtonPaginator
{
    private static final Button first = Button.secondary("first", Emoji.fromUnicode("⏪"));
    private static final Button previous = Button.secondary("previous", Emoji.fromUnicode("⬅"));
    private static final Button next = Button.secondary("next", Emoji.fromUnicode("➡"));
    private static final Button last = Button.secondary("last", Emoji.fromUnicode("⏩"));
    private static final Button delete = Button.danger("stop", Emoji.fromUnicode("\uD83D\uDDD1"));

    private final EventWaiter waiter;
    private final int itemsPerPage;
    private final int pages;
    private final long timeout;
    private final String[] items;
    private final JDA jda;
    private final Set<Long> allowedUsers;
    private final boolean numbered;
    private final String title;
    private final Color color;
    private final String footer;

    private int page = 1;
    private boolean interactionStopped = false;

    private ButtonPaginator(EventWaiter waiter, long timeout, String[] items, JDA jda,
                            Set<Long> allowedUsers, int itemsPerPage, boolean numberedItems, String title, Color color, String footer)
    {
        this.waiter = waiter;
        this.timeout = timeout;
        this.items = items;
        this.jda = jda;
        this.allowedUsers = Collections.unmodifiableSet(allowedUsers);
        this.itemsPerPage = itemsPerPage;
        this.numbered = numberedItems;
        this.title = title;
        this.color = color;
        this.footer = footer;
        this.pages = (int) Math.ceil((double) items.length / itemsPerPage);
    }

    public void paginate(Message message, int page)
    {
        this.page = page;
        if (title == null)
            message.editMessageEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                    .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        else
            message.editMessage(title).setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                    .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
    }

    public void paginate(MessageAction messageAction, int page)
    {
        this.page = page;
        if (title == null)
            messageAction.setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                    .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        else
            messageAction.content(title).setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                    .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
    }

    public void paginate(WebhookMessageAction<Message> action, int page)
    {
        this.page = page;
        if (title == null)
            action.addEmbeds(getEmbed(page)).addActionRows(getButtonLayout(page))
                    .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        else
            action.setContent(title).addEmbeds(getEmbed(page)).addActionRows(getButtonLayout(page))
                    .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
    }

    private ActionRow getButtonLayout(int page)
    {
        if (pages > 2)
            return ActionRow.of(
                    page <= 1 ? first.asDisabled() : first,
                    page <= 1 ? previous.asDisabled() : previous,
                    page >= pages ? next.asDisabled() : next,
                    page >= pages ? last.asDisabled() : last,
                    delete);
        else
            return ActionRow.of(
                    page <= 1 ? previous.asDisabled() : previous,
                    page >= pages ? next.asDisabled() : next,
                    delete);
    }

    private void waitForEvent(long channelId, long messageId)
    {
        waiter.waitForEvent(
                ButtonClickEvent.class,
                event ->
                {
                    if (interactionStopped) return false;
                    if (messageId != event.getMessageIdLong()) return false;
                    if (allowedUsers.size() >= 1)
                    {
                        if (!allowedUsers.contains(event.getUser().getIdLong()))
                        {
                            event.deferEdit().queue(s -> {}, e -> {});
                            return false;
                        }
                    }
                    return true;
                },
                event ->
                {
                    switch (event.getComponentId())
                    {
                        case "previous" -> {
                            page--;
                            if (page < 1) page = 1;
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                        }
                        case "next" -> {
                            page++;
                            if (page > pages) page = pages;
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                        }
                        case "stop" -> {
                            interactionStopped = true;
                            if (!event.getMessage().isEphemeral())
                                event.getMessage().delete().queue(s -> {}, e -> {});
                            else
                                event.editMessageEmbeds(getEmbed(page)).setActionRows(Collections.emptyList()).queue();
                        }
                        case "first" -> {
                            page = 1;
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                        }
                        case "last" -> {
                            page = pages;
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                        }
                    }
                },
                timeout,
                TimeUnit.SECONDS,
                () ->
                {
                    interactionStopped = true;
                    TextChannel channel = jda.getTextChannelById(channelId);
                    if (channel == null) return;
                    channel.retrieveMessageById(messageId)
                            .flatMap(m -> m.editMessageComponents(Collections.emptyList()))
                            .queue(s -> {}, e -> {});
                }
        );
    }

    private MessageEmbed getEmbed(int page)
    {
        if (page > pages) page = pages;
        if (page < 1) page = 1;
        int start = page == 1 ? 0 : ((page - 1) * itemsPerPage);
        int end = Math.min(items.length, page * itemsPerPage);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
        {
            sb.append(numbered ? "**" + (i + 1) + ".** " : "").append(this.items[i]).append("\n");
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setFooter("Page " + page + "/" + pages + (footer != null ? " • "+footer : ""))
                .setColor(color)
                .setDescription(sb.toString().trim());
        return builder.build();
    }

    public static class Builder
    {
        private final JDA jda;
        private EventWaiter waiter;
        private long timeout = -1;
        private String[] items;
        private final Set<Long> allowedUsers = new HashSet<>();
        private int itemsPerPage = 10;
        private boolean numberItems = true;
        private String title = null;
        private Color color;
        private String footer;

        public Builder(JDA jda)
        {
            this.jda = jda;
        }

        public Builder setEventWaiter(@Nonnull EventWaiter waiter)
        {
            this.waiter = waiter;
            return this;
        }

        public Builder setTimeout(long delay, TimeUnit unit)
        {
            Checks.notNull(unit, "TimeUnit");
            Checks.check(delay > 0, "Timeout must be greater than 0!");
            timeout = unit.toSeconds(delay);
            return this;
        }

        public Builder setItems(String[] items)
        {
            this.items = items;
            return this;
        }

        public Builder addAllowedUsers(Long... userIds)
        {
            allowedUsers.addAll(Set.of(userIds));
            return this;
        }

        public Builder setColor(Color color)
        {
            this.color = color;
            return this;
        }

        public Builder setColor(int color)
        {
            this.color = EmbedUtil.intToColor(color);
            return this;
        }

        public Builder setItemsPerPage(int items)
        {
            Checks.check(items > 0, "Items per page must be at least 1");
            this.itemsPerPage = items;
            return this;
        }

        public Builder useNumberedItems(boolean b)
        {
            this.numberItems = b;
            return this;
        }

        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder setFooter(String footer)
        {
            this.footer = footer;
            return this;
        }

        public ButtonPaginator build()
        {
            Checks.notNull(waiter, "Waiter");
            Checks.check(timeout != -1, "You must set a timeout using #setTimeout()!");
            Checks.noneNull(items, "Items");
            Checks.notEmpty(items, "Items");
            return new ButtonPaginator(waiter, timeout, items, jda, allowedUsers, itemsPerPage, numberItems, title, color == null ? Color.black : color, footer);
        }
    }
}
