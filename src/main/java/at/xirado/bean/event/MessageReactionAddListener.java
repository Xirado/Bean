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
import at.xirado.bean.data.ReactionRole;
import at.xirado.bean.data.database.entity.DiscordGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageReactionAddListener extends ListenerAdapter {
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.isFromGuild())
            return;

        if (event.getUser().isBot()) return;

        Guild guild = event.getGuild();
        long messageId = event.getMessageIdLong();
        EmojiUnion emoji = event.getEmoji();
        String reaction = emoji.getType() == Emoji.Type.UNICODE ? emoji.getAsReactionCode() : emoji.asCustom().getId();

        Bean.getInstance().getVirtualThreadExecutor().submit(() -> {
            DiscordGuild guildData = Bean.getInstance().getRepository()
                    .getGuildRepository().getGuildDataBlocking(guild.getIdLong());

            ReactionRole reactionRole = guildData.getReactionRole(messageId, reaction);
            if (reactionRole == null)
                return;

            Role role = event.getGuild().getRoleById(reactionRole.getRoleId());
            if (role != null && guild.getSelfMember().canInteract(role))
                guild.addRoleToMember(event.getMember(), role).queue();
        });
    }
}
