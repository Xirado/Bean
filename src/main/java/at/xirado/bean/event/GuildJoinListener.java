package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.database.SQLBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class GuildJoinListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Bean.class);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        String name = guild.getName();
        int memberCount = guild.getMemberCount();
        if (isGuildBanned(guild.getIdLong())) {
            log.info("Joined banned guild " + name + " with " + memberCount + " members");
            return;
        }
        log.info("Joined guild " + name + " with " + memberCount + " members");
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        String name = guild.getName();
        int memberCount = guild.getMemberCount();
        if (isGuildBanned(guild.getIdLong())) {
            log.info("Left banned guild " + name + " with " + memberCount + " members");
            return;
        }
        log.info("Left guild " + name + " with " + memberCount + " members");
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        log.info("[DM]: {} ({}): {}", event.getAuthor().getAsTag(), event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
    }

    public static boolean isGuildBanned(long guildId) {
        try (var rs = new SQLBuilder("SELECT 1 from banned_guilds WHERE guild_id = ?", guildId).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            log.error("Could not check if guild " + guildId + " is banned", ex);
            return false;
        }
    }

    public static void banGuild(long guildId, String reason) {
        try {
            new SQLBuilder("INSERT INTO banned_guilds (guild_id, reason) values (?,?) ON DUPLICATE KEY UPDATE reason = ?", guildId, reason, reason).execute();
        } catch (SQLException ex) {
            log.error("Could not ban guild " + guildId, ex);
        }
    }

    public static void unbanGuild(long guildId) {
        try {
            new SQLBuilder("DELETE FROM banned_guilds WHERE guild_id = ?", guildId).execute();
        } catch (SQLException ex) {
            log.error("Could not unban guild " + guildId, ex);
        }
    }
}
