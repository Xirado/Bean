package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.objects.CachedMessage;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildAudioPlayer> audioPlayers;

    public AudioManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.audioPlayers = new ConcurrentHashMap<>();
        DataObject ytConfig = Bean.getInstance().getConfig().optObject("youtube").orElseGet(DataObject::empty);
        String email = ytConfig.getString("email", null);
        String password = ytConfig.getString("password", null);
        DataObject spotify = Bean.getInstance().getConfig().optObject("spotify").orElseGet(DataObject::empty);
        String clientId = spotify.getString("client_id", null);
        String clientSecret = spotify.getString("client_secret", null);
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true, email, password));
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        if (clientId != null && clientSecret != null)
            playerManager.registerSourceManager(new SpotifySourceManager(null, clientId, clientSecret, "US", playerManager));
        Thread t = new Thread(() ->
        {
            while (true) {
                for (GuildAudioPlayer guildAudioPlayer : getAudioPlayers()) {
                    if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                        continue;

                    if (guildAudioPlayer.getPlayer().getPlayingTrack().getDuration() == Long.MAX_VALUE)
                        continue;
                    CachedMessage message = guildAudioPlayer.getOpenPlayer();
                    if (message != null) {
                        if (guildAudioPlayer.getPlayer().isPaused() || guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                            continue;
                        if (guildAudioPlayer.getLastPlayerUpdate() + 5000 > System.currentTimeMillis())
                            continue;

                        OffsetDateTime created = TimeUtil.getTimeCreated(message.getMessageId());
                        if (created.plusMinutes(30).isBefore(OffsetDateTime.now())) {
                            guildAudioPlayer.playerSetup(message.getChannel(), s -> {}, e -> {});
                            continue;
                        }
                        TextChannel channel = message.getChannel();
                        if (channel == null) {
                            guildAudioPlayer.setOpenPlayer(null);
                            continue;
                        }
                        guildAudioPlayer.setLastPlayerUpdate(System.currentTimeMillis());
                        guildAudioPlayer.forcePlayerUpdate();
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
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
