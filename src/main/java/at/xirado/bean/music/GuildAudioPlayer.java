package at.xirado.bean.music;

import at.xirado.bean.Bean;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.utils.MiscUtil;

public class GuildAudioPlayer
{
    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;

    public GuildAudioPlayer(long guildId)
    {
        this.guildId = guildId;
        player = Bean.getInstance().getLavalink().getLink(String.valueOf(guildId)).getPlayer();
        scheduler = new AudioScheduler(player, guildId);
        player.addListener(scheduler);
    }

    public AudioScheduler getScheduler()
    {
        return scheduler;
    }

    public LavalinkPlayer getPlayer()
    {
        return player;
    }

    public long getGuildId()
    {
        return guildId;
    }
}
