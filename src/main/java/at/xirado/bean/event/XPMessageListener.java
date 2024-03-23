package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.slashcommands.leveling.XPAlertCommand;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.RoleReward;
import at.xirado.bean.data.database.entity.DiscordGuild;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class XPMessageListener extends ListenerAdapter {
    public static final long TIMEOUT = Bean.getInstance().isDebug() ? 0L : 60000;
    private final ConcurrentHashMap<Long, Long> timeouts = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild())
            return;

        User user = event.getAuthor();

        if (event.isWebhookMessage() || user.isBot() || event.getMessage().getType().isSystem())
            return;

        long userId = user.getIdLong();

        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();

        GuildMessageChannel guildChannel = event.getGuildChannel();

        Bean.getInstance().getVirtualThreadExecutor().submit(() -> {
            DiscordGuild guildData = Bean.getInstance().getRepository()
                    .getGuildRepository()
                    .getGuildDataBlocking(guildId);

            if (guildData.getSilentLevelingChannels().contains(guildChannel.getIdLong()))
                return;

            if (timeouts.containsKey(userId) && System.currentTimeMillis() - timeouts.get(userId) < TIMEOUT)
                return;

            timeouts.put(userId, System.currentTimeMillis());

            try (var connection = Bean.getInstance().getDatabase().getConnectionFromPool()) {
                // Current total amount of xp
                long currentTotalXP = RankingSystem.getTotalXP(connection, guildId, userId);
                // Current level
                int level = RankingSystem.getLevel(currentTotalXP);
                // Current relative xp for next level
                long currentXP = currentTotalXP - RankingSystem.getTotalXPNeeded(level);
                // Amount of xp left to level up
                long xpLeft = RankingSystem.getXPToLevelUp(level);
                // Generate random amount of xp between 15 and 25
                int xpAmount = 15 + random.nextInt(11);

                RankingSystem.addXP(connection, guildId, userId, xpAmount, user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());

                if (xpAmount + currentXP < xpLeft)
                    return;

                try {
                    XPAlertCommand.sendXPAlert(event.getMember(), level + 1, guildChannel);
                } catch (InsufficientPermissionException ignored) {
                }

                RoleReward reward = guildData.getRoleReward(level + 1);

                if (reward == null)
                    return;

                Member selfMember = guild.getSelfMember();

                if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
                    Util.sendDM(guild.getOwnerIdLong(), EmbedUtil.errorEmbed("Hey! You have set up role-rewards in your guild **" + event.getGuild().getName() + "**, but i do not have the **Manage Roles** permission!\nPlease make sure to give me this permission, so i can assign them!"));
                    return;
                }

                Role role = guild.getRoleById(reward.getRoleId());
                if (role == null)
                    return;

                if (!selfMember.canInteract(role)) {
                    Util.sendDM(event.getGuild().getOwnerIdLong(), EmbedUtil.errorEmbed("Hey! You have set up role-rewards in your guild **" + event.getGuild().getName() + "**, but the role **" + role.getName() + "** is above me in the role hierarchy!\nPlease make sure to move the role above me, so i can assign them!"));
                    return;
                }

                event.getGuild().addRoleToMember(user, role).queue();
                RoleReward oldReward = guildData.getLastRoleReward(level);

                if (oldReward == null)
                    return;

                if (oldReward.getRemoveOnNextReward()) {
                    Role oldRole = event.getGuild().getRoleById(oldReward.getRoleId());
                    if (oldRole != null)
                        event.getGuild().removeRoleFromMember(user, oldRole).queue();
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        });
    }
}
