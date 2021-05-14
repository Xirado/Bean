package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class XPListener extends ListenerAdapter
{

    private static final ConcurrentHashMap<Long, Long> timeout = new ConcurrentHashMap<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
    {
        Bean.getInstance().scheduledExecutorService.submit(() -> {
            if(event.getAuthor().isBot() || event.isWebhookMessage()) return;
            if(event.getMessage().getContentRaw().startsWith(Bean.getInstance().prefixManager.getPrefix(event.getGuild().getIdLong()))) return;
            long userID = event.getAuthor().getIdLong();
            long guildID = event.getGuild().getIdLong();
            if(timeout.containsKey(userID))
            {
                long lastXPAdditionAgo = System.currentTimeMillis()-timeout.get(userID);
                if(lastXPAdditionAgo > 60000)
                {
                    Connection connection = Database.getConnectionFromPool();
                    if(connection == null) return;
                    long currentTotalXP = RankingSystem.getTotalXP(connection, guildID, userID);
                    int level = RankingSystem.getLevel(currentTotalXP);
                    long currentXP = currentTotalXP-RankingSystem.getTotalXPNeeded(level);
                    long xpLeft = RankingSystem.getXPToLevelUp(level);
                    int xpAmount = 15+new Random().nextInt(11);
                    RankingSystem.addXP(connection, guildID, userID, xpAmount);
                    Util.closeQuietly(connection);
                    if(xpAmount+currentXP >= xpLeft)
                    {
                        event.getChannel().sendMessage("Hey "+event.getAuthor().getAsMention()+", you've just ranked up to **Level "+(level+1)+"**!").queue();
                    }
                    timeout.put(userID, System.currentTimeMillis());
                }
            }else {
                Connection connection = Database.getConnectionFromPool();
                if(connection == null) return;
                long currentTotalXP = RankingSystem.getTotalXP(connection, guildID, userID);
                int level = RankingSystem.getLevel(currentTotalXP);
                long currentXP = currentTotalXP-RankingSystem.getTotalXPNeeded(level);
                long xpLeft = RankingSystem.getXPToLevelUp(level);
                int xpAmount = 15+new Random().nextInt(11);
                RankingSystem.addXP(connection, guildID, userID, xpAmount);
                Util.closeQuietly(connection);
                if(xpAmount+currentXP >= xpLeft)
                {
                    event.getChannel().sendMessage("Hey "+event.getAuthor().getAsMention()+", you've just ranked up to **Level "+(level+1)+"**!").queue();
                }
                timeout.put(userID, System.currentTimeMillis());
            }
        });
    }
}
