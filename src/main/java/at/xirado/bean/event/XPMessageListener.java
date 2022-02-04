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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class XPMessageListener extends ListenerAdapter
{


    public static final long TIMEOUT = Bean.getInstance().isDebug() ? 0L : 60000;

    private static final Logger LOGGER = LoggerFactory.getLogger(XPMessageListener.class);
    private static final ConcurrentHashMap<Long, Long> timeout = new ConcurrentHashMap<>();

    private static final Random RANDOM = new Random();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (!event.isFromGuild())
            return;
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        if (event.getAuthor().isBot() || event.isWebhookMessage() || event.getMessage().getType().isSystem()) return;
        if (event.getMessage().getContentRaw().startsWith(GuildManager.getGuildData(event.getGuild()).getPrefix()))
            return;
        GuildData data = GuildManager.getGuildData(event.getGuild());
        DataObject dataObject = data.toData();
        DataArray disabledChannels = dataObject.optArray("no_xp_channels").orElse(DataArray.empty());
        if (disabledChannels.stream(DataArray::getString).anyMatch(x -> x.equals(event.getChannel().getId())))
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
                    try (Connection connection = Database.getConnectionFromPool())
                    {
                        if (connection == null) return;
                        long currentTotalXP = RankingSystem.getTotalXP(connection, guildID, userID);
                        int level = RankingSystem.getLevel(currentTotalXP);
                        long currentXP = currentTotalXP - RankingSystem.getTotalXPNeeded(level);
                        long xpLeft = RankingSystem.getXPToLevelUp(level);
                        int xpAmount = 15 + RANDOM.nextInt(11);
                        RankingSystem.addXP(connection, guildID, userID, xpAmount, event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getAuthor().getEffectiveAvatarUrl());
                        Util.closeQuietly(connection);
                        if (xpAmount + currentXP >= xpLeft)
                        {
                            try
                            {
                                XPAlertCommand.sendXPAlert(event.getMember(), level + 1, event.getChannel());
                            } catch (InsufficientPermissionException ignored)
                            {
                            }
                            if (data.hasRoleReward(level + 1))
                            {
                                RoleReward reward = data.getRoleReward(level + 1);
                                Role role = event.getGuild().getRoleById(reward.getRoleId());
                                if (role != null)
                                {
                                    event.getGuild().addRoleToMember(userID, role).queue(s -> {
                                    }, e -> {
                                    });
                                    RoleReward oldReward = data.getLastRoleReward(level);
                                    if (oldReward != null)
                                    {
                                        if (oldReward.doesRemoveOnNextReward())
                                        {
                                            Role oldRole = event.getGuild().getRoleById(oldReward.getRoleId());
                                            if (oldRole != null)
                                            {
                                                event.getGuild().removeRoleFromMember(userID, oldRole).queue(s -> {
                                                }, e -> {
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
                try (Connection connection = Database.getConnectionFromPool())
                {
                    if (connection == null) return;
                    long currentTotalXP = RankingSystem.getTotalXP(connection, guildID, userID);
                    int level = RankingSystem.getLevel(currentTotalXP);
                    long currentXP = currentTotalXP - RankingSystem.getTotalXPNeeded(level);
                    long xpLeft = RankingSystem.getXPToLevelUp(level);
                    int xpAmount = 15 + RANDOM.nextInt(11);
                    RankingSystem.addXP(connection, guildID, userID, xpAmount, event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getAuthor().getEffectiveAvatarUrl());
                    Util.closeQuietly(connection);
                    if (xpAmount + currentXP >= xpLeft)
                    {
                        try
                        {
                            XPAlertCommand.sendXPAlert(event.getMember(), level + 1, event.getChannel());
                        } catch (InsufficientPermissionException ignored)
                        {
                        }
                        if (data.hasRoleReward(level + 1))
                        {
                            RoleReward reward = data.getRoleReward(level + 1);
                            Role role = event.getGuild().getRoleById(reward.getRoleId());
                            if (role != null)
                            {
                                event.getGuild().addRoleToMember(userID, role).queue(s -> {
                                }, e -> {
                                });
                                RoleReward oldReward = data.getLastRoleReward(level);
                                if (oldReward != null)
                                {
                                    if (oldReward.doesRemoveOnNextReward())
                                    {
                                        Role oldRole = event.getGuild().getRoleById(oldReward.getRoleId());
                                        if (oldRole != null)
                                        {
                                            event.getGuild().removeRoleFromMember(userID, oldRole).queue(s -> {
                                            }, e -> {
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
        });
    }
}
