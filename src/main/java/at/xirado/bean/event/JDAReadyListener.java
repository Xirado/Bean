package at.xirado.bean.event;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDAReadyListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);
    private boolean ready = false;
    private boolean lavalinkReady = false;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (!ready)
            Bean.getInstance().getInteractionHandler().init();
        ready = true;
        Bean.getInstance().getInteractionHandler().updateGuildCommands(event.getGuild());
    }
}
