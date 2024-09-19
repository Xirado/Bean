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
import at.xirado.bean.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        if (!event.isFromGuild())
            return;

        if (Bean.getInstance().isDebug() && Bean.WHITELISTED_USERS.stream().noneMatch(x -> x == event.getUser().getIdLong())) {
            event.reply(CommandContext.ERROR_EMOTE + " Bot is in debug mode! Only whitelisted users can execute commands!").setEphemeral(true).queue();
            return;
        }

        Bean.getInstance().getInteractionHandler().handleCommand(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild())
            return;

        Bean.getInstance().getInteractionHandler().handleAutocomplete(event);
    }
}
