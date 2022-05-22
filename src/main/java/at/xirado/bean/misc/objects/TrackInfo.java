package at.xirado.bean.misc.objects;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TrackInfo {
    private final long requester;
    private final long channelId;
    private final long guildId;
    private Set<Long> voteSkips;
    private boolean fromPlaylist = false;
    private String trackUrl = null;
    private String playlistUrl = null;
    private String playlistName = null;

    public TrackInfo(long requester, long guildId, long channelId) {
        this.requester = requester;
        this.guildId = guildId;
        this.channelId = channelId;
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
        if (voteSkips == null) {
            this.voteSkips = new HashSet<>();
            this.voteSkips.add(userId);
            return this;
        }
        this.voteSkips.add(userId);
        return this;
    }

    public TrackInfo removeVoteSkip(long userId) {
        if (voteSkips == null)
            return this;
        this.voteSkips.remove(userId);
        return this;
    }

    public Set<Long> getVoteSkips() {
        return voteSkips == null ? new HashSet<>() : voteSkips;
    }

    public long getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return Bean.getInstance().getShardManager().getGuildById(guildId);
    }
}
