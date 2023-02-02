package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.CachedMessage;
import at.xirado.bean.misc.objects.TrackInfo;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class AudioScheduler extends AudioEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(AudioScheduler.class);

    private final AudioPlayer player;
    private final List<AudioTrack> lastTracks;
    private final BlockingDeque<AudioTrack> queue;
    private final GuildAudioPlayer guildAudioPlayer;
    private final long guildId;
    private boolean repeat = false;
    private boolean shuffle = false;
    private AudioTrack lastTrack;
    private long trackStartTime = 0;

    public AudioScheduler(AudioPlayer player, long guildId, GuildAudioPlayer guildAudioPlayer) {
        this.guildId = guildId;
        this.player = player;
        this.lastTracks = Collections.synchronizedList(new ArrayList<>());
        this.queue = new LinkedBlockingDeque<>();
        this.guildAudioPlayer = guildAudioPlayer;
    }

    public void prevTrack() {
        if (lastTracks.isEmpty() && player.getPlayingTrack() != null) {
            player.getPlayingTrack().setPosition(0);
            return;
        }

        AudioTrack currentTrack = player.getPlayingTrack();

        if (currentTrack != null)
            this.queue.offerFirst(currentTrack.makeClone());

        player.playTrack(lastTracks.get(lastTracks.size() - 1).makeClone());
        lastTracks.remove(lastTracks.size() - 1);
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() != null)
            queue.offer(track);
        else
            player.playTrack(track);
    }

    public AudioTrack nextTrack() {
        if (repeat) {
            AudioTrack track = lastTrack.makeClone();
            player.playTrack(track);
            return track;
        }

        lastTracks.add(lastTrack);
        AudioTrack track = queue.poll();
        if (track == null) {
            AudioChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getSelfMember().getVoiceState().getChannel();
            if (current instanceof StageChannel stageChannel) {
                if (stageChannel.getStageInstance() != null)
                    stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(null)).queue();
            }
        }
        if (track != null)
            player.playTrack(track);
        else
            player.stopTrack();

        return track;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public List<AudioTrack> getLastTracks() {
        return lastTracks;
    }

    public void addLastTrack(AudioTrack... tracks) {
        lastTracks.addAll(Arrays.asList(tracks));
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        lastTrack = track;
        AudioChannel current = Bean.getInstance().getShardManager().getGuildById(guildId).getSelfMember().getVoiceState().getChannel();

        if (current instanceof StageChannel stageChannel) {
            if (stageChannel.getStageInstance() == null) {
                stageChannel.createStageInstance(MusicUtil.getStageTopicString(track)).queue();
            } else {
                stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(track)).queue();
            }
        }

        long duration = System.currentTimeMillis() - trackStartTime;

        if (duration > 5000 && guildAudioPlayer.getOpenPlayer() != null) {
            CachedMessage message = guildAudioPlayer.getOpenPlayer();
            GuildMessageChannel channel = message.getChannel();
            if (channel == null) {
                guildAudioPlayer.setOpenPlayer(null);
                return;
            }
            channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(track)).queue(null, (e) -> guildAudioPlayer.setOpenPlayer(null));
        }

        trackStartTime = System.currentTimeMillis();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.debug("Track {} stopped with reason {}", track.getInfo().title, endReason);
        if (!endReason.mayStartNext)
            return;

        AudioTrack next = nextTrack();

        if (next == null && guildAudioPlayer.getOpenPlayer() != null) {
            CachedMessage message = guildAudioPlayer.getOpenPlayer();
            GuildMessageChannel channel = message.getChannel();
            if (channel == null) {
                guildAudioPlayer.setOpenPlayer(null);
                return;
            }

            channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(null)).queue(null, (e) -> guildAudioPlayer.setOpenPlayer(null));
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if (repeat)
            repeat = false;
        nextTrack();
        TrackInfo info = track.getUserData(TrackInfo.class);
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
        if (guild == null)
            return;
        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, info.getChannelId());
        if (channel == null)
            return;
        log.warn("(Guild: {}) An error occurred while playing track \"{}\" by \"{}\"", guildId, track.getInfo().title, track.getInfo().author, exception);
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        queue.clear();
        lastTracks.clear();
        lastTrack = null;
    }
}
