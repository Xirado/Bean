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
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class MessageCreateListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild())
            return;
        Metrics.MESSAGES.labels("messages").inc();
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        Member member = event.getMember();
        if (member == null) return;
        String content = event.getMessage().getContentRaw();
        String[] args = content.split("\\s+");
        if (args.length == 1
                && event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser())
                && !event.getMessage().getMentions().mentionsEveryone()
                && event.getMessage().getReferencedMessage() == null) {
            event.getMessage().reply("<a:ping:818580038949273621>")
                    .mentionRepliedUser(false).queue();
            return;
        }

        if (content.startsWith(event.getJDA().getSelfUser().getAsMention())) {
            Bean.getInstance().getCommandHandler().handleCommandFromGuild(event);
        }
    }
}
