package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.CachedMessage;
import at.xirado.bean.misc.objects.TrackInfo;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioScheduler extends PlayerEventListenerAdapter
{

    private static final Logger log = LoggerFactory.getLogger(AudioScheduler.class);

    private final LavalinkPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final GuildAudioPlayer guildAudioPlayer;
    private final long guildId;
    private boolean repeat = false;
    private boolean shuffle = false;
    private AudioTrack lastTrack;

    public AudioScheduler(LavalinkPlayer player, long guildId, GuildAudioPlayer guildAudioPlayer)
    {
        this.guildId = guildId;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.guildAudioPlayer = guildAudioPlayer;
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
            AudioChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getSelfMember().getVoiceState().getChannel();
            if (current instanceof StageChannel stageChannel)
            {
                if (stageChannel.getStageInstance() != null)
                    stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(null)).queue();
            }
        }
        if (guildAudioPlayer.getOpenPlayer() != null && track == null)
        {
            CachedMessage message = guildAudioPlayer.getOpenPlayer();
            TextChannel channel = message.getChannel();
            if (channel == null)
            {
                guildAudioPlayer.setOpenPlayer(null);
                return;
            }

            channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(null)).queue(null, (e) -> guildAudioPlayer.setOpenPlayer(null));
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

    public boolean isShuffle()
    {
        return shuffle;
    }

    public void setShuffle(boolean shuffle)
    {
        this.shuffle = shuffle;
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
        AudioChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getSelfMember().getVoiceState().getChannel();
        if (current instanceof StageChannel stageChannel)
        {
            if (stageChannel.getStageInstance() == null)
            {
                stageChannel.createStageInstance(MusicUtil.getStageTopicString(track)).queue();
            }
            else
            {
                stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(track)).queue();
            }
        }
        if (guildAudioPlayer.getOpenPlayer() != null)
        {
            CachedMessage message = guildAudioPlayer.getOpenPlayer();
            TextChannel channel = message.getChannel();
            if (channel == null)
            {
                guildAudioPlayer.setOpenPlayer(null);
                return;
            }

            channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(track)).queue(null, (e) -> guildAudioPlayer.setOpenPlayer(null));
        }

    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        log.debug("Track {} stopped with reason {}", track.getInfo().title, endReason);
        if (endReason.mayStartNext)
        {
            nextTrack();
        }
        if (endReason == AudioTrackEndReason.STOPPED || endReason == AudioTrackEndReason.LOAD_FAILED)
        {
            if (guildAudioPlayer.getOpenPlayer() != null)
            {
                CachedMessage message = guildAudioPlayer.getOpenPlayer();
                TextChannel channel = message.getChannel();
                if (channel == null)
                {
                    guildAudioPlayer.setOpenPlayer(null);
                    return;
                }

                channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(null)).queue(null, (e) -> guildAudioPlayer.setOpenPlayer(null));
            }
        }
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception)
    {
        if (repeat)
            repeat = false;
        nextTrack();
        TrackInfo info = track.getUserData(TrackInfo.class);
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
        if (guild == null)
            return;
        TextChannel channel = guild.getTextChannelById(info.getChannelId());
        if (channel == null)
            return;
        if (exception instanceof FriendlyException friendlyException)
        {
            if (friendlyException.severity != FriendlyException.Severity.COMMON)
            {
                log.warn("(Guild: {}) An error occurred while playing track \"{}\" by \"{}\"", guildId, track.getInfo().title, track.getInfo().author, exception);
            }
        }
        else
        {
            log.warn("(Guild: {}) An error occurred while playing track \"{}\" by \"{}\"", guildId, track.getInfo().title, track.getInfo().author, exception);
        }
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
