package at.xirado.bean.prometheus;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.ThreadPoolExecutor;

public class MetricsJob extends Thread
{
    public MetricsJob()
    {
        setDaemon(true);
        setName("Metrics-Job");
    }

    @Override
    public void run()
    {
        byte runs = 0;

        Bean instance = Bean.getInstance();

        while (true)
        {
            int busyCommandThreads = ((ThreadPoolExecutor) instance.getCommandExecutor()).getActiveCount();
            int totalCommandThreads = ((ThreadPoolExecutor) instance.getCommandExecutor()).getCorePoolSize();

            int activePlayers = instance.getAudioManager().getAudioPlayers()
                    .stream().mapToInt(player -> player.getPlayer().getPlayingTrack() != null ? 1 : 0)
                    .sum();

            Metrics.BUSY_THREADS.labels("command").set(busyCommandThreads);
            Metrics.BUSY_THREADS.labels("command_total").set(totalCommandThreads);
            Metrics.PLAYING_MUSIC_PLAYERS.set(activePlayers);

            runs++;

            if (runs == 12) // per minute
            {
                int guildCount = (int) instance.getShardManager().getGuildCache().size();

                int userCount = instance.getShardManager().getGuildCache()
                        .stream()
                        .mapToInt(Guild::getMemberCount)
                        .sum();

                instance.getShardManager().setPresence(OnlineStatus.ONLINE, Activity.watching(userCount + " users | bean.bz"));

                Metrics.USER_COUNT.set(userCount);
                Metrics.GUILD_COUNT.set(guildCount);

                runs = 0;
            }
            try
            {
                Thread.sleep(5000);
            } catch (InterruptedException ignored)
            {
            }
        }
    }
}
