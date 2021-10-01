package at.xirado.bean.misc;

import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MusicUtil
{

    public static String getStageTopicString(AudioTrack track)
    {
        if (track == null)
            return "Queue is empty!";
        return "Playing " + track.getInfo().title + (track.getInfo().author == null ? "" : " by " + track.getInfo().author);
    }

    public static MessageEmbed getAddedToQueueMessage(GuildAudioPlayer player, AudioTrack track)
    {
        AudioPlayer audioPlayer = player.getPlayer();
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x452350);
        if (audioPlayer.getPlayingTrack() == null)
        {
            builder.setDescription("**Now playing** " + Util.titleMarkdown(track) + " (**" + FormatUtil.formatTime(track.getDuration()) + "**)");
        } else
        {
            builder.setDescription("**Added** " + Util.titleMarkdown(track) + " **to the queue!** (**" + FormatUtil.formatTime(track.getDuration()) + "**)");
        }
        if (track instanceof YoutubeAudioTrack)
        {
            builder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
        }
        return builder.build();
    }
}
