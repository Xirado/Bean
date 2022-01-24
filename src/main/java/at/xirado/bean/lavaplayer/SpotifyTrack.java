package at.xirado.bean.lavaplayer;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

public class SpotifyTrack extends DelegatedAudioTrack{

    private final String isrc;
    private final String artworkURL;
    private final SpotifyAudioSource spotifyAudioSource;

    public SpotifyTrack(String title, String identifier, String isrc, Image[] images, String uri, ArtistSimplified[] artists, Integer trackDuration, SpotifyAudioSource spotifySourceManager){
        this(new AudioTrackInfo(title,
                artists.length == 0 ? "unknown" : artists[0].getName(),
                trackDuration.longValue(),
                identifier,
                false,
                "https://open.spotify.com/track/" + identifier
        ), isrc, images.length == 0 ? null : images[0].getUrl(), spotifySourceManager);
    }

    public SpotifyTrack(AudioTrackInfo trackInfo, String isrc, String artworkURL, SpotifyAudioSource spotifySourceManager){
        super(trackInfo);
        this.isrc = isrc;
        this.artworkURL = artworkURL;
        this.spotifyAudioSource = spotifySourceManager;
    }

    public static SpotifyTrack of(TrackSimplified track, Album album, SpotifyAudioSource spotifySourceManager){
        return new SpotifyTrack(track.getName(), track.getId(), null, album.getImages(), track.getUri(), track.getArtists(), track.getDurationMs(), spotifySourceManager);
    }

    public static SpotifyTrack of(Track track, SpotifyAudioSource spotifySourceManager){
        return new SpotifyTrack(track.getName(), track.getId(), track.getExternalIds().getExternalIds().getOrDefault("isrc", null), track.getAlbum().getImages(), track.getUri(), track.getArtists(), track.getDurationMs(), spotifySourceManager);
    }

    public String getISRC(){
        return this.isrc;
    }

    public String getArtworkURL(){
        return this.artworkURL;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) {}

    @Override
    public AudioSourceManager getSourceManager(){
        return this.spotifyAudioSource;
    }

    @Override
    protected AudioTrack makeShallowClone(){
        return new SpotifyTrack(trackInfo, isrc, artworkURL, this.spotifyAudioSource);
    }

}