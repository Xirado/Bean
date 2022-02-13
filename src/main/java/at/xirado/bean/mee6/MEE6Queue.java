package at.xirado.bean.mee6;

import at.xirado.bean.Bean;
import at.xirado.bean.data.MEE6Player;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class MEE6Queue extends Thread
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MEE6Queue.class);
    private static final int DELAY_MS = 1200;

    private final Queue<MEE6Request> queue = new PriorityBlockingQueue<>();
    private final OkHttpClient client = new OkHttpClient();

    private long rateLimitExpiry = 0;
    private long currentRequestGuildId = 0;
    private boolean isRateLimit = false;

    public MEE6Queue()
    {
        setName("MEE6 Worker");
        setDaemon(true);
        setUncaughtExceptionHandler((t, e) -> LOGGER.error("Error occurred on MEE6-Worker thread!", e));
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                if (queue.isEmpty())
                {
                    Thread.sleep(100);
                    continue;
                }
                long currentTime = System.currentTimeMillis();

                if (isRateLimit)
                {
                    if (rateLimitExpiry > currentTime)
                    {
                        Thread.sleep(100);
                        continue;
                    }
                    else
                    {
                        isRateLimit = false;
                        rateLimitExpiry = 0;
                    }
                }

                if (!queue.isEmpty())
                {
                    makeCall(queue.poll());
                }

                Thread.sleep(DELAY_MS);
            }
            catch (InterruptedException ignored) {}
        }
    }

    public void addRequest(MEE6Request request)
    {
        queue.add(request);
    }

    public Queue<MEE6Request> getQueue()
    {
        return queue;
    }

    private void makeCall(MEE6Request request)
    {
        currentRequestGuildId = request.getGuildId();
        try
        {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("mee6.xyz")
                    .setPath("/api/plugins/levels/leaderboard/" + request.getGuildId())
                    .addParameter("page", String.valueOf(request.getPage()))
                    .build();

            Request httpRequest = new Request.Builder()
                    .url(uri.toURL())
                    .get()
                    .build();

            Call call = client.newCall(httpRequest);

            Response response = call.execute();

            if (response.code() == 429)
            {
                String retryAfter = response.header("retry-after");
                if (retryAfter != null)
                    LOGGER.warn("Encountered MEE6 Cloudflare Rate-limit! Retrying after {} seconds", retryAfter);
                else
                    LOGGER.warn("Encountered MEE6 Cloudflare Rate-limit without \"retry-after\" header! Retrying after 1 hour");
                isRateLimit = true;
                int retryAfterMillis = retryAfter == null ? 60 * 60 * 1000 : Integer.parseInt(retryAfter) * 1000;
                this.rateLimitExpiry = System.currentTimeMillis() + retryAfterMillis;
                queue.offer(request);
                response.close();
                return;
            }

            if (response.code() == 404)
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(request.getGuildId());
                if (guild != null)
                    Util.sendDM(request.getAuthorId(), EmbedUtil.defaultEmbed("Hey! We tried to migrate MEE6 experience for your guild **" + guild.getName() + "**, but sadly we could not find anything!\n\nAdding MEE6 to your server again often fixes this issue!"));
                response.close();
                return;
            }

            if (response.code() == 401)
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(request.getGuildId());
                if (guild != null)
                    Util.sendDM(request.getAuthorId(), EmbedUtil.defaultEmbed("Hey! We tried to migrate MEE6 experience for your guild **" + guild.getName() + "**, but your servers MEE6 leaderboard is [set to private]](https://mee6.xyz/dashboard/" + guild.getIdLong() + "/leaderboard)!\n\nPlease set it to public and try again!"));
                response.close();
                return;
            }

            if (String.valueOf(response.code()).charAt(0) == '5')
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(request.getGuildId());
                if (guild != null)
                    Util.sendDM(request.getAuthorId(), EmbedUtil.defaultEmbed("Hey! We tried to migrate MEE6 experience for your guild **" + guild.getName() + "**, but the MEE6 server appears to be having issues!\n\nPlease try again later."));
                response.close();
                return;
            }

            if (!response.isSuccessful())
            {
                LOGGER.error("MEE6 returned error {}\n{}", response.code(), response.body().string());
                response.close();
                return;
            }

            DataObject object = DataObject.fromJson(response.body().string());
            response.close();

            DataArray playersArray = object.getArray("players");

            int entries = playersArray.length();

            try (Connection connection = Database.getConnectionFromPool())
            {
                playersArray
                        .stream(DataArray::getObject)
                        .map(MEE6Player::fromData)
                        .forEach(player -> updateXP(connection, player, request.getGuildId()));
            }
            catch (SQLException e)
            {
                LOGGER.error("SQLException occurred!", e);
            }

            if (entries == 100)
            {
                addRequest(new MEE6Request(request.getGuildId(), request.getAuthorId()).setPage(request.getPage() + 1));
                return;
            }
            int entriesTotal = ((request.getPage() - 1) * 100) + entries;
            LOGGER.debug("Finished transferring xp for guild " + request.getGuildId() + "! Migrated " + entriesTotal + "users.");
            Guild guild = Bean.getInstance().getShardManager().getGuildById(request.getGuildId());
            if (guild != null && entriesTotal > 1)
                Util.sendDM(request.getAuthorId(), EmbedUtil.defaultEmbed("Hey! We'd like you to know that we have finished migrating MEE6 xp for all users that have a Rank on your guild **" + guild.getName() + "**! (" + entriesTotal + " Users)"));
            else if (guild != null && entriesTotal == 0)
                Util.sendDM(request.getAuthorId(), EmbedUtil.defaultEmbed("Hey! We tried to migrate MEE6 xp for all users on your guild, but we couldn't find any!"));
        }
        catch (URISyntaxException | IOException exception)
        {
            LOGGER.error("Error occurred in MEE6Queue!", exception);
        }
    }

    private void updateXP(Connection connection, MEE6Player player, long guildId)
    {
        long id = Long.parseLong(player.getId());
        long xp = player.getXp();
        String name = player.getUsername();
        String discriminator = player.getDiscriminator();
        String avatarUrl = player.getAvatar();
        RankingSystem.setXP(connection, guildId, id, xp, name, discriminator, avatarUrl);
    }

    public boolean hasPendingRequest(long guildId)
    {
        if (currentRequestGuildId == guildId)
            return true;
        return queue.stream()
                .anyMatch(request -> request.getGuildId() == guildId);
    }
}
