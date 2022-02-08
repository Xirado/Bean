package at.xirado.bean.music;

import at.xirado.bean.misc.Metrics;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.CachedMessage;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.time.OffsetDateTime;
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
        Thread t = new Thread(() ->
        {
            while (true)
            {
                for (GuildAudioPlayer guildAudioPlayer : getAudioPlayers())
                {
                    if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                        continue;

                    if (guildAudioPlayer.getPlayer().getPlayingTrack().getDuration() == Long.MAX_VALUE)
                        continue;
                    CachedMessage message = guildAudioPlayer.getOpenPlayer();
                    if (message != null)
                    {
                        if (guildAudioPlayer.getPlayer().isPaused() || guildAudioPlayer.getPlayer().getPlayingTrack() == null) continue;
                        OffsetDateTime created = TimeUtil.getTimeCreated(message.getMessageId());
                        if (OffsetDateTime.now().plusMinutes(55).isBefore(created))
                        {
                            System.out.println("message is 55 minutes old! Making new one");
                            guildAudioPlayer.playerSetup(message.getChannel(), s -> {}, e -> {});
                            continue;
                        }
                        TextChannel channel = message.getChannel();
                        if (channel == null)
                        {
                            guildAudioPlayer.setOpenPlayer(null);
                            continue;
                        }
                        channel.editMessageEmbedsById(message.getMessageId(), MusicUtil.getPlayerEmbed(guildAudioPlayer.getPlayer().getPlayingTrack())).queue(null, e -> guildAudioPlayer.setOpenPlayer(null));
                    }
                }
                int playingAudioPlayers = getAudioPlayers().stream()
                        .mapToInt(pl -> pl.getPlayer().getPlayingTrack() == null ? 0 : 1)
                        .sum();
                Metrics.PLAYING_MUSIC_PLAYERS.set(playingAudioPlayers);
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException ignored)
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
