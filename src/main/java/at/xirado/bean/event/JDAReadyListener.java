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
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDAReadyListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);
    private boolean ready = false;

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        Metrics.EVENTS.inc();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (!ready)
            Bean.getInstance().getInteractionHandler().init();

        Bean.getInstance().getInteractionHandler().updateGuildCommands(event.getGuild());

        if (ready)
            return;

        ready = true;

        LOGGER.info("Successfully started {} shards!", Bean.getInstance().getShardManager().getShards().size());
        if (Bean.getInstance().isDebug())
            LOGGER.warn("Development mode enabled.");
    }
}
