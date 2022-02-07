package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.objects.CachedMessage;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.TextChannel;

public class GuildAudioPlayer
{
    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;
    private final JdaLink link;

    private CachedMessage openPlayer;

    public GuildAudioPlayer(long guildId)
    {
        this.guildId = guildId;
        link = Bean.getInstance().getLavalink().getLink(String.valueOf(guildId));
        player = link.getPlayer();
        scheduler = new AudioScheduler(player, guildId, this);
        player.addListener(scheduler);
        openPlayer = null;
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

    public CachedMessage getOpenPlayer()
    {
        return openPlayer;
    }

    public void setOpenPlayer(CachedMessage openPlayer)
    {
        if (this.openPlayer != null)
        {
            TextChannel channel = this.openPlayer.getChannel();
            if (channel != null)
                channel.deleteMessageById(this.openPlayer.getMessageId()).queue(s -> {}, e -> {});
        }
        this.openPlayer = openPlayer;
    }

    public void destroy()
    {
        Bean.getInstance().getAudioManager().removePlayer(this);
        link.destroy();
        scheduler.destroy();
    }
}
