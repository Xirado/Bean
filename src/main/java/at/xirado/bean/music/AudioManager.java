package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.CachedMessage;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioManager {
    public static final String[] SPOTIFY_PROVIDERS = {
            "dzisrc:%ISRC%",
            "ytsearch:\"%ISRC%\"",
            "ytsearch:%QUERY%",
            "scsearch:%QUERY%"
    };

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildAudioPlayer> audioPlayers;
    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor(
                    Util.newThreadFactory("Player Updater", LoggerFactory.getLogger(AudioManager.class), true)
            );

    public AudioManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.audioPlayers = new ConcurrentHashMap<>();
        String deezerKey = Bean.getInstance().getConfig().getString("deezer_key", null);
        DataObject ytConfig = Bean.getInstance().getConfig().optObject("youtube").orElseGet(DataObject::empty);
        String email = ytConfig.getString("email", null);
        String password = ytConfig.getString("password", null);
        DataObject spotify = Bean.getInstance().getConfig().optObject("spotify").orElseGet(DataObject::empty);
        String clientId = spotify.getString("client_id", null);
        String clientSecret = spotify.getString("client_secret", null);
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true, email, password));
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        if (deezerKey != null)
            playerManager.registerSourceManager(new DeezerAudioSourceManager(deezerKey));
        if (clientId != null && clientSecret != null)
            playerManager.registerSourceManager(new SpotifySourceManager(SPOTIFY_PROVIDERS, clientId, clientSecret, "US", playerManager));

        executorService.scheduleWithFixedDelay(() -> {
            for (GuildAudioPlayer guildAudioPlayer : getAudioPlayers()) {
                AudioPlayer player = guildAudioPlayer.getPlayer();

                if (player.getPlayingTrack() == null || player.isPaused() || player.getPlayingTrack().getDuration() == Long.MAX_VALUE)
                    continue;

                CachedMessage message = guildAudioPlayer.getOpenPlayer();

                if (message == null)
                    continue;

                OffsetDateTime created = TimeUtil.getTimeCreated(message.getMessageId());
                GuildMessageChannel channel = message.getChannel();

                if (channel == null)
                    continue;

                if (created.plusMinutes(30).isBefore(OffsetDateTime.now())) {
                    guildAudioPlayer.playerSetup(channel, null, null);
                    continue;
                }

                guildAudioPlayer.forcePlayerUpdate();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public synchronized GuildAudioPlayer getAudioPlayer(long guildId) {
        if (audioPlayers.containsKey(guildId))
            return audioPlayers.get(guildId);
        GuildAudioPlayer player = new GuildAudioPlayer(playerManager, guildId);
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
        guild.getAudioManager().setSendingHandler(player.getSendHandler());
        audioPlayers.put(guildId, player);
        return player;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public Set<GuildAudioPlayer> getAudioPlayers() {
        return Set.copyOf(audioPlayers.values());
    }

    public void removePlayer(GuildAudioPlayer player) {
        audioPlayers.remove(player.getGuildId());
    }
}
