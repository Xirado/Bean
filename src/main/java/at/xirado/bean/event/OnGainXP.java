package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.slashcommands.leveling.XPAlertCommand;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.RoleReward;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class OnGainXP extends ListenerAdapter
{


    public static final long TIMEOUT = Bean.getInstance().isDebug() ? 0L : 60000;

    private static final Logger LOGGER = LoggerFactory.getLogger(OnGainXP.class);
    private static final ConcurrentHashMap<Long, Long> timeout = new ConcurrentHashMap<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
    {
        if (event.getAuthor().isBot() || event.isWebhookMessage() || event.getMessage().getType().isSystem()) return;
        if (event.getMessage().getContentRaw().startsWith(GuildManager.getGuildData(event.getGuild()).getPrefix()))
            return;
        Bean.getInstance().getExecutor().submit(() ->
        {
            long userID = event.getAuthor().getIdLong();
            long guildID = event.getGuild().getIdLong();
            if (timeout.containsKey(userID))
            {
                long lastXPAdditionAgo = System.currentTimeMillis() - timeout.get(userID);
                if (lastXPAdditionAgo > TIMEOUT)
                {
                    try(Connection connection = Database.getConnectionFromPool())
                    {
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
                            GuildData data = GuildManager.getGuildData(event.getGuild());
                            if (data.hasRoleReward(level + 1))
                            {
                                RoleReward reward = data.getRoleReward(level + 1);
                                Role role = event.getGuild().getRoleById(reward.getRoleId());
                                if (role != null)
                                {
                                    event.getGuild().addRoleToMember(userID, role).queue(s ->
                                    {
                                    }, e ->
                                    {
                                    });
                                    RoleReward oldReward = data.getLastRoleReward(level);
                                    if (oldReward != null)
                                    {
                                        if (oldReward.doesRemoveOnNextReward())
                                        {
                                            Role oldRole = event.getGuild().getRoleById(oldReward.getRoleId());
                                            if (oldRole != null)
                                            {
                                                event.getGuild().removeRoleFromMember(userID, oldRole).queue(s ->
                                                {
                                                }, e ->
                                                {
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        timeout.put(userID, System.currentTimeMillis());
                    } catch (Exception ex)
                    {
                        LOGGER.error("Could not update XP!", ex);
                    }
                }
            } else
            {
                try(Connection connection = Database.getConnectionFromPool())
                {
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
                        GuildData data = GuildManager.getGuildData(event.getGuild());
                        if (data.hasRoleReward(level + 1))
                        {
                            RoleReward reward = data.getRoleReward(level + 1);
                            Role role = event.getGuild().getRoleById(reward.getRoleId());
                            if (role != null)
                            {
                                event.getGuild().addRoleToMember(userID, role).queue(s ->
                                {
                                }, e ->
                                {
                                });
                                RoleReward oldReward = data.getLastRoleReward(level);
                                if (oldReward != null)
                                {
                                    if (oldReward.doesRemoveOnNextReward())
                                    {
                                        Role oldRole = event.getGuild().getRoleById(oldReward.getRoleId());
                                        if (oldRole != null)
                                        {
                                            event.getGuild().removeRoleFromMember(userID, oldRole).queue(s ->
                                            {
                                            }, e ->
                                            {
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                    timeout.put(userID, System.currentTimeMillis());
                }catch (Exception ex)
                {
                    LOGGER.error("Could not update XP!", ex);
                }
            }
        });
    }
}
