/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.RoleReward;
import at.xirado.bean.data.database.entity.DiscordGuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GuildMemberJoinListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            return;

        Bean.getInstance().getVirtualThreadExecutor().submit(() -> {
            DiscordGuild guildData = Bean.getInstance()
                    .getRepository()
                    .getGuildRepository()
                    .getGuildDataBlocking(event.getGuild().getIdLong());

            long totalXP = RankingSystem.getTotalXP(event.getGuild().getIdLong(), event.getUser().getIdLong());
            int level = RankingSystem.getLevel(totalXP);

            if (level > 0) {
                Set<RoleReward> roleRewards = guildData.getEffectiveRoleRewards(level);
                Set<Role> rolesToAdd = new HashSet<>();
                for (RoleReward reward : roleRewards) {
                    if (reward.getPersistant()) {
                        Role role = event.getGuild().getRoleById(reward.getRoleId());
                        if (role != null && event.getGuild().getSelfMember().canInteract(role))
                            rolesToAdd.add(role);
                    }
                }
                for (Role role : rolesToAdd)
                    event.getGuild().addRoleToMember(event.getMember(), role).queue(); // Need to do this because of possible race condition with modifyMemberRoles()
            }
        });
    }
}
