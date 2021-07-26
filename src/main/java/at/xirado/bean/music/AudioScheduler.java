package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioScheduler extends AudioEventAdapter
{
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final long guildId;
    private boolean repeat = false;
    private AudioTrack playingSong;

    public AudioScheduler(AudioPlayer player, long guildId)
    {
        this.guildId = guildId;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track)
    {
        if (!player.startTrack(track, true))
        {
            queue.offer(track);
        }
    }

    public void nextTrack()
    {
        if (repeat)
        {
            player.startTrack(playingSong.makeClone(), false);
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
        player.startTrack(track, false);

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

    public AudioPlayer getPlayer()
    {
        return player;
    }

    @Override
    public void onPlayerPause(AudioPlayer player)
    {
        super.onPlayerPause(player);
    }

    @Override
    public void onPlayerResume(AudioPlayer player)
    {
        super.onPlayerResume(player);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track)
    {
        super.onTrackStart(player, track);
        playingSong = track;
        VoiceChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getAudioManager().getConnectedChannel();
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
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        if (endReason.mayStartNext)
            nextTrack();

    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception)
    {
        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs)
    {
        super.onTrackStuck(player, track, thresholdMs);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace)
    {
        super.onTrackStuck(player, track, thresholdMs, stackTrace);
    }

    @Override
    public void onEvent(AudioEvent event)
    {
        super.onEvent(event);
    }

    public long getGuildId()
    {
        return guildId;
    }
}
