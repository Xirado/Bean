package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.misc.FormatUtil;
import at.xirado.bean.misc.Util;
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
        return "Now playing: "+track.getInfo().title+" by "+track.getInfo().author;
    }

    public static MessageEmbed getAddedToQueueMessage(GuildAudioPlayer player, AudioTrack track)
    {
        AudioPlayer audioPlayer = player.getPlayer();
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x452350);
        if (audioPlayer.getPlayingTrack() == null)
        {
            builder.setDescription("**Now playing**: " + Util.titleMarkdown(track) + " (`" + FormatUtil.formatTime(track.getDuration()) + "`)");
        } else
        {
            builder.setDescription("**Added to queue**: " + Util.titleMarkdown(track) + " (`" + FormatUtil.formatTime(track.getDuration()) + "`)");
        }
        if (track instanceof YoutubeAudioTrack)
        {
            builder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
        }
        String author = track.getInfo().author;
        if (author != null)
            builder.setFooter("Source: " + author);
        return builder.build();
    }
}
