package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.log.MCColor;
import at.xirado.bean.log.Shell;
import at.xirado.bean.music.GuildAudioPlayer;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Shutdown extends ConsoleCommand
{

    private static final Logger logger = LoggerFactory.getLogger(Shutdown.class);

    public Shutdown()
    {
        this.invoke = "shutdown";
        this.description = "Shuts down all JDA instances and all threadpools";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        Set<GuildAudioPlayer> audioPlayers = Bean.getInstance().getAudioManager().getAudioPlayers();
        if (args.length == 0 || !args[0].equalsIgnoreCase("-f"))
        {
            int playing = audioPlayers.stream()
                    .mapToInt(pl -> pl.getPlayer().getPlayingTrack() == null ? 0 : 1)
                    .sum();

            int mee6QueueSize = Bean.getInstance().getMEE6Queue().getQueue().size();

            if (playing > 0)
                Shell.println(MCColor.translate('&', "&eCareful: There are &2" + playing + "&e players running! Use shutdown -f to force-shutdown."));
            if (mee6QueueSize > 0)
                Shell.println(MCColor.translate('&', "&eCareful: There are &2" + mee6QueueSize + "&e pending MEE6 requests!"));
            if (playing > 0 || mee6QueueSize > 0)
                return;
        }
        logger.info("Shutting down...");
        logger.info("Deleting player messages...");
        audioPlayers.forEach(pl -> {
            if (pl.getOpenPlayer() != null)
                pl.getOpenPlayer().delete().complete();
        });
        logger.info("Destroying players...");
        audioPlayers.forEach(GuildAudioPlayer::destroy);
        Bean.getInstance().getShardManager().shutdown();
        logger.info("Awaiting JDA ShardManager shutdown...");
        for (JDA jda : Bean.getInstance().getShardManager().getShards())
        {
            while (jda.getStatus() != JDA.Status.SHUTDOWN)
            {
                Thread.onSpinWait();
            }
        }
        logger.info("Stopped all shards");
        logger.info("Goodbye");
        System.exit(0);
    }
}
