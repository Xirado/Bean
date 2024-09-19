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

public class BookmarkDismissableContent implements MessageEmbedDismissable{

    @NotNull
    @Override
    public String getIdentifier() {
        return "bookmark";
    }

    @Nullable
    @Override
    public String getMediaUrl() {
        return "https://bean.bz/assets/content/bookmark.png";
    }

    @NotNull
    @Override
    public MessageEmbed get() {
        return new EmbedBuilder()
                .setTitle("Did you know?")
                .setDescription("Bookmark songs and playlists using the **/bookmark** command!\nHaving to always type the link to your favourite youtube or spotify playlist is annoying, isn't it?")
                .setImage(getMediaUrl())
                .setColor(getEmbedColor())
                .build();
    }
}
