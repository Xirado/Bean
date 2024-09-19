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

package at.xirado.bean.data.content;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankCustomBackgroundDismissableContent implements MessageEmbedDismissable {

    @NotNull
    @Override
    public String getIdentifier() {
        return "rankcustombackground";
    }

    @Nullable
    @Override
    public String getMediaUrl() {
        return "https://bean.bz/assets/content/rank_custom_backgrounds.png";
    }

    @NotNull
    @Override
    public MessageEmbed get() {
        return new EmbedBuilder()
                .setTitle("Did you know?")
                .setDescription("You can set your own custom background on your rank card using `/setxpcard`!")
                .setImage(getMediaUrl())
                .setColor(getEmbedColor())
                .build();
    }
}
