package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.CachedMessage;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.function.Consumer;

public class GuildAudioPlayer {
    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final JdaLink link;
    private final long guildId;

    private CachedMessage openPlayer;
    private long lastPlayerUpdate;

    public GuildAudioPlayer(long guildId) {
        this.guildId = guildId;
        link = Bean.getInstance().getLavalink().getLink(String.valueOf(guildId));
        player = link.getPlayer();
        scheduler = new AudioScheduler(player, guildId, this);
        player.addListener(scheduler);
        openPlayer = null;
        lastPlayerUpdate = 0;
    }

    public AudioScheduler getScheduler() {
        return scheduler;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    public long getGuildId() {
        return guildId;
    }

    public JdaLink getLink() {
        return link;
    }

    public CachedMessage getOpenPlayer() {
        return openPlayer;
    }

    public void setOpenPlayer(CachedMessage openPlayer) {
        if (this.openPlayer != null) {
            TextChannel channel = this.openPlayer.getChannel();
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
        channel.sendMessageEmbeds(MusicUtil.getPlayerEmbed(track == null ? player.getPlayingTrack() : track))
                .setActionRows(MusicUtil.getPlayerButtons(this))
                .queue(message -> {
                    setOpenPlayer(new CachedMessage(message));
                    onSuccess.accept(message);
                }, onError);
    }

    public void forcePlayerUpdate() {
        CachedMessage message = getOpenPlayer();
        if (message == null || message.getChannel() == null)
            return;

        TextChannel channel = message.getChannel();
        channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(player.getPlayingTrack()))
                .setActionRows(MusicUtil.getPlayerButtons(this)).queue(null, e -> setOpenPlayer(null));

    }

    public void forcePlayerComponentsUpdate() {
        CachedMessage message = getOpenPlayer();
        if (message == null || message.getChannel() == null)
            return;

        TextChannel channel = message.getChannel();
        channel.editMessageComponentsById(message.getMessageId(), MusicUtil.getPlayerButtons(this))
                .queue(null, e -> setOpenPlayer(null));
    }

    public void destroy() {
        setOpenPlayer(null);
        Bean.getInstance().getAudioManager().removePlayer(this);
        link.destroy();
        scheduler.destroy();
    }

    public synchronized long getLastPlayerUpdate() {
        return lastPlayerUpdate;
    }

    public synchronized void setLastPlayerUpdate(long lastPlayerUpdate) {
        this.lastPlayerUpdate = lastPlayerUpdate;
    }
}
