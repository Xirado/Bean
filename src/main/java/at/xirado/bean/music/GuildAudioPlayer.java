package at.xirado.bean.music;

import at.xirado.bean.Bean;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuildAudioPlayer
{
    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;
    private final JdaLink link;
    private final List<InteractionHook> openPlayers;

    public GuildAudioPlayer(long guildId)
    {
        this.guildId = guildId;
        link = Bean.getInstance().getLavalink().getLink(String.valueOf(guildId));
        player = link.getPlayer();
        scheduler = new AudioScheduler(player, guildId, this);
        player.addListener(scheduler);
        openPlayers = Collections.synchronizedList(new ArrayList<>());
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

    public JdaLink getLink()
    {
        return link;
    }

    public List<InteractionHook> getOpenPlayers()
    {
        return openPlayers;
    }

    public void destroy()
    {
        Bean.getInstance().getAudioManager().removePlayer(this);
        link.destroy();
        scheduler.destroy();
    }
}
