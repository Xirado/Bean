package at.xirado.bean.event;

import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.misc.objects.RoleReward;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuildMemberJoinListener extends ListenerAdapter
{
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event)
    {
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        GuildData guildData = GuildManager.getGuildData(event.getGuild());
        long totalXP = RankingSystem.getTotalXP(event.getGuild().getIdLong(), event.getUser().getIdLong());
        int level = RankingSystem.getLevel(totalXP);
        if (level > 0)
        {
            List<RoleReward> roleRewards = guildData.getEffectiveRoleRewards(level);
            Set<Role> rolesToAdd = new HashSet<>(event.getMember().getRoles());
            for (RoleReward reward : roleRewards)
            {
                if (reward.isPersistant())
                {
                    Role role = event.getGuild().getRoleById(reward.getRoleId());
                    if (role != null && event.getGuild().getSelfMember().canInteract(role))
                    {
                        rolesToAdd.add(role);
                    }
                }
            }
            if (!rolesToAdd.isEmpty())
            {
                event.getGuild().modifyMemberRoles(event.getMember(), rolesToAdd).queue(s -> {}, e -> {});
            }
        }
    }
}
