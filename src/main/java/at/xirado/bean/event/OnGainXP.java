package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.slashcommands.XPAlertCommand;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class OnGainXP extends ListenerAdapter
{

    private static final ConcurrentHashMap<Long, Long> timeout = new ConcurrentHashMap<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
    {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if (event.getMessage().getContentRaw().startsWith(GuildManager.getGuildData(event.getGuild()).getPrefix()))
            return;
        Bean.getInstance().getExecutor().submit(() ->
        {
            long userID = event.getAuthor().getIdLong();
            long guildID = event.getGuild().getIdLong();
            if (timeout.containsKey(userID))
            {
                long lastXPAdditionAgo = System.currentTimeMillis() - timeout.get(userID);
                if (lastXPAdditionAgo > 60000)
                {
                    Connection connection = Database.getConnectionFromPool();
                    if (connection == null) return;
                    long currentTotalXP = RankingSystem.getTotalXP(connection, guildID, userID);
                    int level = RankingSystem.getLevel(currentTotalXP);
                    long currentXP = currentTotalXP - RankingSystem.getTotalXPNeeded(level);
                    long xpLeft = RankingSystem.getXPToLevelUp(level);
                    int xpAmount = 15 + new Random().nextInt(11);
                    RankingSystem.addXP(connection, guildID, userID, xpAmount, event.getAuthor().getName(), event.getAuthor().getDiscriminator());
                    Util.closeQuietly(connection);
                    if (xpAmount + currentXP >= xpLeft)
                    {
                        XPAlertCommand.sendXPAlert(event.getMember(), level + 1, event.getChannel());
                    }
                    timeout.put(userID, System.currentTimeMillis());
                }
            } else
            {
                Connection connection = Database.getConnectionFromPool();
                if (connection == null) return;
                long currentTotalXP = RankingSystem.getTotalXP(connection, guildID, userID);
                int level = RankingSystem.getLevel(currentTotalXP);
                long currentXP = currentTotalXP - RankingSystem.getTotalXPNeeded(level);
                long xpLeft = RankingSystem.getXPToLevelUp(level);
                int xpAmount = 15 + new Random().nextInt(11);
                RankingSystem.addXP(connection, guildID, userID, xpAmount, event.getAuthor().getName(), event.getAuthor().getDiscriminator());
                Util.closeQuietly(connection);
                if (xpAmount + currentXP >= xpLeft)
                {
                    XPAlertCommand.sendXPAlert(event.getMember(), level + 1, event.getChannel());
                }
                timeout.put(userID, System.currentTimeMillis());
            }
        });
    }
}
