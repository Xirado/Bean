package at.xirado.bean.lavaplayer;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class SpotifyTrack extends DelegatedAudioTrack
{

    private final String isrc;
    private final String artworkURL;
    private final SpotifyAudioSource spotifyAudioSource;

    public SpotifyTrack(AudioTrackInfo trackInfo, String isrc, String artworkURL, SpotifyAudioSource spotifySourceManager)
    {
        super(trackInfo);
        this.isrc = isrc;
        this.artworkURL = artworkURL;
        this.spotifyAudioSource = spotifySourceManager;
    }

    public String getISRC()
    {
        return this.isrc;
    }

    public String getArtworkURL()
    {
        return this.artworkURL;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor)
    {
    }

    @Override
    public AudioSourceManager getSourceManager()
    {
        return this.spotifyAudioSource;
    }

    @Override
    protected AudioTrack makeShallowClone()
    {
        return new SpotifyTrack(trackInfo, isrc, artworkURL, this.spotifyAudioSource);
    }

}
