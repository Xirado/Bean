package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDAReadyListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);
    private boolean ready = false;

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        Metrics.EVENTS.inc();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (!ready)
            Bean.getInstance().getInteractionHandler().init();

        Bean.getInstance().getInteractionHandler().updateGuildCommands(event.getGuild());

        if (ready)
            return;

        ready = true;

        LOGGER.info("Successfully started {} shards!", Bean.getInstance().getShardManager().getShards().size());
        if (Bean.getInstance().isDebug())
            LOGGER.warn("Development mode enabled.");
    }
}
