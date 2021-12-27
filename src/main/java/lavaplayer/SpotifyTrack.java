package lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import lavalink.client.LavalinkUtil;

public class SpotifyTrack extends DelegatedAudioTrack
{
    private final SpotifyAudioSource source;
    private String isrc;

    public SpotifyTrack(String title, String identifier, ArtistSimplified[] artists, Integer trackDuration, SpotifyAudioSource source)
    {
        this(new AudioTrackInfo(title, artists[0].getName(), trackDuration.longValue(), identifier, false, "https://open.spotify.com/track/" + identifier), source);
    }

    public SpotifyTrack(AudioTrackInfo trackInfo, SpotifyAudioSource source)
    {
        super(trackInfo);
        this.source = source;
    }

    public SpotifyTrack setIsrc(String isrc)
    {
        this.isrc = isrc;
        return this;
    }

    public String getISRC()
    {
        return isrc;
    }

    public boolean hasISRC()
    {
        return isrc != null;
    }

    public static SpotifyTrack of(TrackSimplified track, SpotifyAudioSource source)
    {
        return new SpotifyTrack(track.getName(), track.getId() != null ? track.getId() : track.getUri(), track.getArtists(), track.getDurationMs(), source);
    }

    public static SpotifyTrack of(Track track, SpotifyAudioSource source)
    {
        return new SpotifyTrack(track.getName(), track.getId() != null ? track.getId() : track.getUri(), track.getArtists(), track.getDurationMs(), source);
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception
    {
        AudioItem delegate = getDelegate();

        if (delegate == null)
            throw new RuntimeException("No matching youtube track found");

        if (delegate instanceof AudioPlaylist)
            delegate = ((AudioPlaylist) delegate).getTracks().get(0);

        if (delegate instanceof InternalAudioTrack)
        {
            processDelegate((InternalAudioTrack) delegate, executor);
            return;
        }

        throw new RuntimeException("No matching youtube track found");
    }

    @Override
    public AudioSourceManager getSourceManager()
    {
        return this.source;
    }

    private boolean hasResult(AudioItem item)
    {
        return item instanceof AudioTrack || item instanceof AudioPlaylist;
    }

    private AudioItem getDelegate()
    {
        AudioPlayerManager apm = LavalinkUtil.getPlayerManager();
        YoutubeAudioSourceManager asm = apm.source(YoutubeAudioSourceManager.class);
        AudioItem audioItem = null;
        if (hasISRC())
            audioItem = asm.loadItem(apm, new AudioReference("ytsearch:\"" + getISRC() + "\"", null));
        if (!hasResult(audioItem))
            audioItem = asm.loadItem(apm, new AudioReference("ytsearch:" + trackInfo.title + " " + trackInfo.author, null));
        if (!hasResult(audioItem))
            return null;
        return audioItem;
    }
}
