package at.xirado.bean.music;

import at.xirado.bean.misc.Metrics;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager
{
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildAudioPlayer> audioPlayers;

    public AudioManager()
    {
        this.playerManager = new DefaultAudioPlayerManager();
        this.audioPlayers = new ConcurrentHashMap<>();
        Thread t = new Thread(() -> {
            while (true)
            {
                int playingAudioPlayers = getAudioPlayers().stream()
                        .mapToInt(pl -> pl.getPlayer().getPlayingTrack() == null ? 0 : 1)
                        .sum();
                Metrics.PLAYING_MUSIC_PLAYERS.set(playingAudioPlayers);
                try
                {
                    Thread.sleep(15000);
                } catch (InterruptedException ignored)
                {
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public synchronized GuildAudioPlayer getAudioPlayer(long guildId)
    {
        if (audioPlayers.containsKey(guildId))
            return audioPlayers.get(guildId);
        GuildAudioPlayer player = new GuildAudioPlayer(guildId);
        audioPlayers.put(guildId, player);
        return player;
    }

    public Set<GuildAudioPlayer> getAudioPlayers()
    {
        return Set.copyOf(audioPlayers.values());
    }

    public void removePlayer(GuildAudioPlayer player)
    {
        audioPlayers.remove(player.getGuildId());
    }
}
