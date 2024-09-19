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
