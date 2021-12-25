package lavaplayer;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import lavalink.client.LavalinkUtil;

public class SpotifyTrack extends DelegatedAudioTrack {

    public SpotifyAudioSource source;

    public SpotifyTrack(String title, String identifier, ArtistSimplified[] artists, Integer trackDuration, SpotifyAudioSource source) {
        this(new AudioTrackInfo(title, artists[0].getName(), trackDuration.longValue(), identifier, false, "https://open.spotify.com/track/" + identifier), source);
    }

    public SpotifyTrack(AudioTrackInfo trackInfo, SpotifyAudioSource source) {
        super(trackInfo);
        this.source = source;
    }

    public static SpotifyTrack of(TrackSimplified track, SpotifyAudioSource source) {
        return new SpotifyTrack(track.getName(), track.getId(), track.getArtists(), track.getDurationMs(), source);
    }

    public static SpotifyTrack of(Track track, SpotifyAudioSource source) {
        return new SpotifyTrack(track.getName(), track.getId(), track.getArtists(), track.getDurationMs(), source);
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        AudioItem track = LavalinkUtil.getPlayerManager().source(YoutubeAudioSourceManager.class).loadItem(LavalinkUtil.getPlayerManager(), new AudioReference("ytsearch:" + trackInfo.title + " " + trackInfo.author, null));
        if (track == null) {
            throw new RuntimeException("No matching youtube track found");
        }
        if (track instanceof AudioPlaylist) {
            track = ((AudioPlaylist) track).getTracks().get(0);
        }
        if (track instanceof InternalAudioTrack) {
            processDelegate((InternalAudioTrack) track, executor);
            return;
        }
        throw new RuntimeException("No matching youtube track found");
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return this.source;
    }

}
