package at.xirado.bean.interactions;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ButtonPaginator
{
    private static final Button previous = Button.secondary("previous", Emoji.fromUnicode("⬅"));
    private static final Button stop = Button.danger("stop", Emoji.fromUnicode("⏹"));
    private static final Button next = Button.secondary("next", Emoji.fromUnicode("➡"));

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

    private int page = 1;
    private boolean interactionStopped = false;

    private ButtonPaginator(EventWaiter waiter, long timeout, String[] items, JDA jda,
                            Set<Long> allowedUsers, int itemsPerPage, boolean numberedItems, String title, Color color)
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
        this.pages = (int) Math.ceil((double) items.length / itemsPerPage);
    }

    public void paginate(Message m, int page)
    {
        this.page = page;
        if (title == null)
            m.editMessageEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page)).queue(this::waitForEvent, e -> waitForEvent(m));
        else
            m.editMessage(title).setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page)).queue(this::waitForEvent, e -> waitForEvent(m));
    }

    private ActionRow getButtonLayout(int page)
    {
        return ActionRow.of(page <= 1 ? previous.asDisabled() : previous, stop, page >= pages ? next.asDisabled() : next);
    }

    private void waitForEvent(Message m)
    {
        final long channelId = m.getChannel().getIdLong();
        final long messageId = m.getIdLong();
        waiter.waitForEvent(
                ButtonClickEvent.class,
                event ->
                {
                    if (interactionStopped) return false;
                    if (messageId != event.getMessageIdLong()) return false;
                    if (allowedUsers.size() >= 1)
                        return allowedUsers.contains(event.getUser().getIdLong());
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
                            waitForEvent(event.getMessage());
                        }
                        case "next" -> {
                            page++;
                            if (page > pages) page = pages;
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getMessage());
                        }
                        case "stop" -> {
                            interactionStopped = true;
                            jda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(
                                    (message) ->
                                            message.editMessageComponents(Collections.emptyList()).queue()
                                    ,
                                    (error) ->
                                    {
                                    })
                            ;
                        }
                    }
                },
                timeout,
                TimeUnit.SECONDS,
                () ->
                {
                    interactionStopped = true;
                    jda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(
                            message ->
                                    message.editMessageComponents(Collections.emptyList()).queue(s -> {}, e -> {}),
                            error -> {}
                    );
                }
        );
    }

    private MessageEmbed getEmbed(int page)
    {
        if (page > pages) page = pages;
        int start = page == 1 ? 0 : ((page - 1) * itemsPerPage);
        int end = Math.min(items.length, page * itemsPerPage);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
        {
            sb.append(numbered ? "`" + (i + 1) + ".` " : "").append(this.items[i]).append("\n");
        }
        return new EmbedBuilder()
                .setFooter("Page " + page + "/" + pages)
                .setColor(color)
                .setDescription(sb.toString().trim())
                .build();
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

        public ButtonPaginator build()
        {
            Checks.notNull(waiter, "Waiter");
            Checks.check(timeout != -1, "You must set a timeout using #setTimeout()!");
            Checks.noneNull(items, "Items");
            Checks.notEmpty(items, "Items");
            return new ButtonPaginator(waiter, timeout, items, jda, allowedUsers, itemsPerPage, numberItems, title, color == null ? Color.black : color);
        }
    }
}
