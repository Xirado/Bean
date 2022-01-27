package at.xirado.bean.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildAudioPlayer> audioPlayers;

    public AudioManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.audioPlayers = new ConcurrentHashMap<>();
    }

    public synchronized GuildAudioPlayer getAudioPlayer(long guildId) {
        if (audioPlayers.containsKey(guildId))
            return audioPlayers.get(guildId);
        GuildAudioPlayer player = new GuildAudioPlayer(guildId);
        audioPlayers.put(guildId, player);
        return player;
    }

    public Set<GuildAudioPlayer> getAudioPlayers() {
        return Set.copyOf(audioPlayers.values());
    }

    public void removePlayer(GuildAudioPlayer player) {
        audioPlayers.remove(player.getGuildId());
    }
}
