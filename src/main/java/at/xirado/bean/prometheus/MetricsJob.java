package at.xirado.bean.prometheus;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.ThreadPoolExecutor;

public class MetricsJob extends Thread {
    public MetricsJob() {
        setDaemon(true);
        setName("Metrics-Job");
    }

    @Override
    public void run() {
        byte runs = 0;

        Bean instance = Bean.getInstance();

        while (true) {
            int busyCommandThreads = ((ThreadPoolExecutor) instance.getCommandExecutor()).getActiveCount();
            int totalCommandThreads = ((ThreadPoolExecutor) instance.getCommandExecutor()).getCorePoolSize();

            Metrics.BUSY_THREADS.labels("command").set(busyCommandThreads);
            Metrics.BUSY_THREADS.labels("command_total").set(totalCommandThreads);
            instance.getShardManager().getShards().forEach(shard -> {
                Metrics.DISCORD_GATEWAY_PING
                        .labels(String.valueOf(shard.getShardInfo().getShardId()))
                        .set(shard.getGatewayPing());
            });

            runs++;

            if (runs == 12) // per minute
            {
                int guildCount = (int) instance.getShardManager().getGuildCache().size();

                int userCount = instance.getShardManager().getGuildCache()
                        .stream()
                        .mapToInt(Guild::getMemberCount)
                        .sum();

                Metrics.USER_COUNT.set(userCount);
                Metrics.GUILD_COUNT.set(guildCount);

                runs = 0;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
