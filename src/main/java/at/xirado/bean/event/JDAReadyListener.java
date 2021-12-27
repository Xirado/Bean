package at.xirado.bean.event;

import at.xirado.bean.Bean;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class JDAReadyListener extends ListenerAdapter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        Bean.getInstance().getExecutor().submit(() -> {
            LOGGER.info("Successfully started "+Bean.getInstance().getShardManager().getShards().size()+" shards!");
            Bean.getInstance().getSlashCommandHandler().initialize();
            if (Bean.getInstance().isDebug())
                LOGGER.warn("Debug mode enabled! Commands will not be executed for users.");
            Bean.getInstance().initCommandCheck();
            JdaLavalink lavalink = Bean.getInstance().getLavalink();
            lavalink.setJdaProvider((shard) -> Bean.getInstance().getShardManager().getShardById(shard));
            lavalink.setUserId(event.getJDA().getSelfUser().getId());
            DataObject config = DataObject.fromJson(Bean.getInstance().getConfig().toJson()); // TODO: Use DataObject instead of LinkedDataObject. This is shitty...
            DataArray nodes = config.optArray("lavalink_nodes").orElse(DataArray.empty());
            nodes.stream(DataArray::getObject).forEach(node -> {
                String url = node.getString("url");
                String password = node.getString("password");
                try
                {
                    lavalink.addNode(new URI(url), password);
                } catch (URISyntaxException e)
                {
                    LOGGER.error("Could not add Lavaink node!", e);
                }
            });
        });
        Bean.getInstance().getExecutor().scheduleAtFixedRate(() -> {
            int memberCount = Bean.getInstance().getShardManager()
                    .getGuildCache()
                    .stream()
                    .mapToInt(Guild::getMemberCount)
                    .sum();
            Bean.getInstance().getShardManager()
                    .getShardCache()
                    .forEach(shard -> shard.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("bean.bz | "+memberCount+" users")));
        }, 0, 5, TimeUnit.MINUTES);
    }
}