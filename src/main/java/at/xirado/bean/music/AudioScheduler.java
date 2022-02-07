package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.TrackInfo;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.*;
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
        if (guildAudioPlayer.getOpenPlayers().size() > 0)
        {
            guildAudioPlayer.getOpenPlayers().forEach(hook -> {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(track.getInfo().title);
                if (track instanceof SpotifyTrack spotifyTrack)
                    builder.setThumbnail(spotifyTrack.getArtworkURL());
                else if (track instanceof YoutubeAudioTrack)
                    builder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
                hook.editOriginalEmbeds(builder.build()).queue(null, (e) -> guildAudioPlayer.getOpenPlayers().remove(hook));
            });
        }

    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        log.debug("Track " + track.getInfo().title + " stopped with reason " + endReason);
        if (endReason.mayStartNext)
            nextTrack();
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
        channel.sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred while playing track **" + track.getInfo().title + "**!\n`" + exception.getCause().getMessage() + "`")).queue();
        log.warn("(Guild: " + guildId + ") An error occurred while playing track \"" + track.getInfo().title + "\" by \"" + track.getInfo().author + "\"", exception);
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
