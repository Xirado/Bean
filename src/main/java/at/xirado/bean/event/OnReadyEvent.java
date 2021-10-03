package at.xirado.bean.event;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnReadyEvent extends ListenerAdapter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        LOGGER.info("Successfully started "+Bean.getInstance().getShardManager().getShards().size()+" shards!");
        Bean.getInstance().getSlashCommandHandler().initialize();
        if (Bean.getInstance().isDebug())
        {
            LOGGER.warn("Debug mode enabled! Commands will not be executed for users.");
        }
        Bean.getInstance().initCommandCheck();
    }
}
