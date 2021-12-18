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
            if (playing > 0)
            {
                Shell.println(MCColor.translate('&', "&eCareful: There are &2"+playing+"&e players running! Use shutdown -f to force-shutdown."));
                return;
            }
        }
        logger.info("Shutting down...");
        Bean.getInstance().getShardManager().shutdown();
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
