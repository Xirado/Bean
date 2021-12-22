package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioScheduler extends PlayerEventListenerAdapter
{
    private final LavalinkPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final long guildId;
    private boolean repeat = false;
    private AudioTrack lastTrack;

    public AudioScheduler(LavalinkPlayer player, long guildId)
    {
        this.guildId = guildId;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track)
    {
        if (player.getPlayingTrack() != null)
            queue.offer(track);
        else
            player.playTrack(track);
    }

    public void nextTrack()
    {
        if (repeat)
        {
            player.playTrack(lastTrack.makeClone());
            return;
        }
        AudioTrack track = queue.poll();
        if (track == null)
        {
            VoiceChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getAudioManager().getConnectedChannel();
            if (current instanceof StageChannel stageChannel)
            {
                if (stageChannel.getStageInstance() != null)
                    stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(null)).queue();
            }
        }
        if (track != null)
            player.playTrack(track);
        else
            player.stopTrack();

    }

    public boolean isRepeat()
    {
        return repeat;
    }

    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }

    public BlockingQueue<AudioTrack> getQueue()
    {
        return queue;
    }

    public LavalinkPlayer getPlayer()
    {
        return player;
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track)
    {
        lastTrack = track;
        VoiceChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getSelfMember().getVoiceState().getChannel();
        if (current instanceof StageChannel stageChannel)
        {
            if (stageChannel.getStageInstance() == null)
            {
                stageChannel.createStageInstance(MusicUtil.getStageTopicString(track)).queue();
            } else
            {
                stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(track)).queue();
            }
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        if (endReason.mayStartNext)
            nextTrack();
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception)
    {
        LoggerFactory.getLogger(Bean.class).error("Error occurred while playing track!", exception);
    }

    public long getGuildId()
    {
        return guildId;
    }

    public void destroy()
    {
        queue.clear();
        lastTrack = null;
    }
}
