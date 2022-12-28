package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.CachedMessage;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.function.Consumer;

public class GuildAudioPlayer {
    private final AudioPlayer player;
    private final AudioScheduler scheduler;
    private final SendHandler sendHandler;
    private final long guildId;

    private CachedMessage openPlayer;
    private long lastPlayerUpdate;

    public GuildAudioPlayer(AudioPlayerManager manager, long guildId) {
        this.guildId = guildId;
        player = manager.createPlayer();
        scheduler = new AudioScheduler(player, guildId, this);
        sendHandler = new SendHandler(player);
        player.addListener(scheduler);
        openPlayer = null;
        lastPlayerUpdate = 0;
    }

    public AudioScheduler getScheduler() {
        return scheduler;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public SendHandler getSendHandler() {
        return sendHandler;
    }

    public long getGuildId() {
        return guildId;
    }

    public CachedMessage getOpenPlayer() {
        return openPlayer;
    }

    public void setOpenPlayer(CachedMessage openPlayer) {
        if (this.openPlayer != null) {
            GuildMessageChannel channel = this.openPlayer.getChannel();
            if (channel != null)
                channel.deleteMessageById(this.openPlayer.getMessageId())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
        this.openPlayer = openPlayer;
    }

    public void playerSetup(GuildMessageChannel channel, Consumer<Message> onSuccess, Consumer<Throwable> onError) {
        playerSetup(channel, null, onSuccess, onError);
    }

    public void playerSetup(GuildMessageChannel channel, AudioTrack track, Consumer<Message> onSuccess, Consumer<Throwable> onError) {
        try {
            channel.sendMessageEmbeds(MusicUtil.getPlayerEmbed(track == null ? player.getPlayingTrack() : track))
                    .setComponents(MusicUtil.getPlayerButtons(this))
                    .queue(message -> {
                        setOpenPlayer(new CachedMessage(message));
                        if (onSuccess != null)
                            onSuccess.accept(message);
                    }, onError != null ? onError : (ex) -> {});
        } catch (Throwable throwable) {
            if (onError != null) onError.accept(throwable);
        }
    }

    public void forcePlayerUpdate() {
        CachedMessage message = getOpenPlayer();
        if (message == null || message.getChannel() == null)
            return;

        GuildMessageChannel channel = message.getChannel();
        channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(player.getPlayingTrack()))
                .setComponents(MusicUtil.getPlayerButtons(this)).queue(null, e -> setOpenPlayer(null));

    }

    public void forcePlayerComponentsUpdate() {
        CachedMessage message = getOpenPlayer();
        if (message == null || message.getChannel() == null)
            return;

        GuildMessageChannel channel = message.getChannel();
        channel.editMessageComponentsById(message.getMessageId(), MusicUtil.getPlayerButtons(this))
                .queue(null, e -> setOpenPlayer(null));
    }

    public void destroy() {
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
        if (guild != null)
            guild.getAudioManager().closeAudioConnection();
        setOpenPlayer(null);
        Bean.getInstance().getAudioManager().removePlayer(this);
        player.destroy();
        scheduler.destroy();
    }

    public synchronized long getLastPlayerUpdate() {
        return lastPlayerUpdate;
    }

    public synchronized void setLastPlayerUpdate(long lastPlayerUpdate) {
        this.lastPlayerUpdate = lastPlayerUpdate;
    }
}
