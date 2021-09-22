package at.xirado.bean.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildAudioPlayer
{
    private final AudioPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;

    public GuildAudioPlayer(AudioPlayerManager manager, long guildId)
    {
        this.guildId = guildId;
        player = manager.createPlayer();
        scheduler = new AudioScheduler(player, guildId);
        player.addListener(scheduler);
    }

    public AudioPlayerSendHandler getSendHandler()
    {
        return new AudioPlayerSendHandler(player);
    }

    public AudioScheduler getScheduler()
    {
        return scheduler;
    }

    public AudioPlayer getPlayer()
    {
        return player;
    }

    public long getGuildId()
    {
        return guildId;
    }
}
