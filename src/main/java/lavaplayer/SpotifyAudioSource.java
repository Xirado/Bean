package lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.readNullableText;
import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.writeNullableText;

public class SpotifyAudioSource implements AudioSourceManager
{
    @Override
    public String getSourceName()
    {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference)
    {
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track)
    {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException
    {
        var spotifyTrack = (SpotifyTrack) track;
        writeNullableText(output, spotifyTrack.getISRC());
        writeNullableText(output, spotifyTrack.getArtworkURL());
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException
    {
        return new SpotifyTrack(trackInfo, readNullableText(input), readNullableText(input), this);
    }

    @Override
    public void shutdown()
    {

    }
}
