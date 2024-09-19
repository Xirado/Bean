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

package at.xirado.bean.misc.objects;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TrackInfo implements SerializableData {
    private final long requester;
    private final long channelId;
    private final long guildId;
    private Set<Long> voteSkips = new HashSet<>();
    private boolean fromPlaylist = false;
    private String trackUrl = null;
    private String playlistUrl = null;
    private String playlistName = null;

    public TrackInfo(long requester, long guildId, long channelId) {
        this.requester = requester;
        this.guildId = guildId;
        this.channelId = channelId;
    }

    public static TrackInfo fromData(DataObject data) {
        TrackInfo info = new TrackInfo(data.getLong("requester"), data.getLong("guild_id"), data.getLong("channel_id"));

        data.optArray("vote_skips").orElseGet(DataArray::empty).stream(DataArray::getLong).forEach(info::addVoteSkip);
        if (!data.isNull("track_url"))
            info.setTrackUrl(data.getString("track_url"));

        if (!data.isNull("playlist_url"))
            info.setPlaylistUrl(data.getString("playlist_url"));

        if (!data.isNull("playlist_name"))
            info.setPlaylistName(data.getString("playlist_name"));

        return info;
    }

    @NotNull
    @Override
    public DataObject toData() {
        return DataObject.empty()
                .put("requester", requester)
                .put("channel_id", channelId)
                .put("guild_id", guildId)
                .put("vote_skips", voteSkips)
                .put("track_url", trackUrl)
                .put("playlist_url", playlistUrl)
                .put("playlist_name", playlistName);
    }

    public long getChannelId() {
        return channelId;
    }

    public long getRequesterIdLong() {
        return requester;
    }

    public TrackInfo setPlaylistUrl(@Nullable String playlistUrl) {
        this.playlistUrl = playlistUrl;
        this.fromPlaylist = playlistUrl != null;
        return this;
    }

    public TrackInfo setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
        return this;
    }

    public TrackInfo setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
        return this;
    }

    public boolean isFromPlaylist() {
        return fromPlaylist;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public TrackInfo addVoteSkip(long userId) {
        this.voteSkips.add(userId);
        return this;
    }

    public TrackInfo removeVoteSkip(long userId) {
        this.voteSkips.remove(userId);
        return this;
    }

    public Set<Long> getVoteSkips() {
        return voteSkips;
    }

    public long getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return Bean.getInstance().getShardManager().getGuildById(guildId);
    }


}
