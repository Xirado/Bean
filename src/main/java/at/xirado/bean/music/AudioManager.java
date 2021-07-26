package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.objects.CachedMessage;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AudioManager
{
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildAudioPlayer> audioPlayers;
    private final Map<Long, CachedMessage> nowPlayingMessages = new HashMap<>();
    private final ShardManager shardManager;

    public AudioManager(ShardManager manager)
    {
        this.shardManager = manager;
        this.playerManager = new DefaultAudioPlayerManager();
        this.audioPlayers = new ConcurrentHashMap<>();
        AudioSourceManagers.registerRemoteSources(playerManager);
        Bean.getInstance().getExecutor().scheduleAtFixedRate(this::updateAll, 0, 5, TimeUnit.SECONDS);
    }

    private void updateAll()
    {
        for (Map.Entry<Long, CachedMessage> entry : nowPlayingMessages.entrySet())
        {
            long guildId = entry.getKey();
            CachedMessage cachedMessage = entry.getValue();
            Guild guild = shardManager.getGuildById(guildId);
            if (guild == null)
            {
                nowPlayingMessages.remove(guildId);
                continue;
            }

        }
    }

    public synchronized GuildAudioPlayer getAudioPlayer(long guildId)
    {
        if (audioPlayers.containsKey(guildId))
            return audioPlayers.get(guildId);
        GuildAudioPlayer player = new GuildAudioPlayer(playerManager, guildId);
        audioPlayers.put(guildId, player);
        return player;
    }

    public AudioPlayerManager getPlayerManager()
    {
        return playerManager;
    }

    public CachedMessage getLastNPMessage(long guildId)
    {
        return nowPlayingMessages.get(guildId);
    }

    public void setLastNPMessage(long guildId, Message message)
    {
        nowPlayingMessages.put(guildId, new CachedMessage(message));
    }
}
