package at.xirado.bean.event;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildJoinListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Bean.class);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        String name = guild.getName();
        int memberCount = guild.getMemberCount();
        log.info("Joined guild {} with {} members", name, memberCount);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        String name = guild.getName();
        int memberCount = guild.getMemberCount();
        log.info("Left guild {} with {} members", name, memberCount);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromGuild())
            return;
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        log.info("[DM]: {} ({}): {}", event.getAuthor().getAsTag(), event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
    }
}
