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

package at.xirado.bean.data;

import net.dv8tion.jda.api.interactions.commands.Command;

public class Bookmark implements IAutocompleteChoice {
    public static final String BOOKMARK_EMOJI = "\uD83D\uDCCC";
    public static final String SCROLL = "\uD83D\uDCDC"; // indicates that this is a playlist

    private final String name; // Title of the video or the playlist
    private final String value; // URL
    private final boolean playlist; // if this is a playlist or not

    public Bookmark(String name, String value, boolean playlist) {
        this.name = name;
        this.value = value.length() > 100 ? value.substring(0, 100) : value;
        this.playlist = playlist;
    }

    public boolean isPlaylist() {
        return playlist;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    public String getFormattedString() {
        String x = BOOKMARK_EMOJI + (playlist ? SCROLL : "") + " " + name;
        if (x.length() > 100) {
            String replaceString = "...";
            String substr = x.substring(0, 100 - replaceString.length());
            x = substr + replaceString;
        }
        return x;
    }

    @Override
    public Command.Choice toCommandAutocompleteChoice() {
        return new Command.Choice(getFormattedString(), value);
    }
}
