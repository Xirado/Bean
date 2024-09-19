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
import at.xirado.bean.data.content.DismissableState;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class DismissableContentButtonListener extends ListenerAdapter {

    private static final MessageEmbed CONFIRMATION_EMBED = EmbedUtil.defaultEmbedBuilder("You may delete this message now.").build();

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        var componentId = event.getComponentId();
        if (!componentId.startsWith("dismissable:"))
            return;
        var identifier = componentId.substring(12);

        Bean.getInstance().getVirtualThreadExecutor().submit(() -> {
            var progress = Bean.getInstance().getDismissableContentManager().getProgress(event.getUser().getIdLong(), identifier, true);
            if (progress == null)
                return;

            progress.setState(DismissableState.ACKNOWLEDGED).update();
            event.editMessageEmbeds(CONFIRMATION_EMBED)
                    .setComponents(Collections.emptyList())
                    .queue();
        });
    }
}
