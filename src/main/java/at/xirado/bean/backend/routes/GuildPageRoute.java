package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.Authenticator;
import at.xirado.bean.backend.DiscordCredentials;
import at.xirado.bean.backend.ObjectBuilder;
import at.xirado.bean.backend.WebServer;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class GuildPageRoute implements Route
{
    @Override
    public Object handle(Request request, Response response) throws Exception
    {
        String authHeader = request.headers("authorization");
        if (authHeader == null || !authHeader.startsWith("Token "))
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        String guildIdString = request.headers("guild_id");
        if (guildIdString == null)
        {
            response.status(400);
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Missing guild id!")
                    .toString();
        }
        String token = authHeader.substring(7);
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        Authenticator authenticator = Bean.getInstance().getAuthenticator();
        if (!authenticator.isAuthenticated(tokenBytes))
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Invalid token (Try logging out and in again)")
                    .toString();
        }
        if (authenticator.isAccessTokenExpired(tokenBytes))
            authenticator.refreshAccessToken(tokenBytes);
        DiscordCredentials credentials = authenticator.getCredentials(tokenBytes);
        String accessToken = credentials.getAccessToken();

        DataObject guilds = WebServer.retrieveGuilds(accessToken);
        if (guilds.isNull("guilds"))
        {
            DataObject object = DataObject.empty();
            object.put("http_code", guilds.getInt("http_code"));
            if (!guilds.isNull("code"))
                object.put("code", guilds.getInt("code"));
            if (!guilds.isNull("message"))
                object.put("message", guilds.getString("message"));
            return object.toString();
        }
        ShardManager shardManager = Bean.getInstance().getShardManager();
        List<Guild> mutualGuilds = guilds.getArray("guilds").stream(DataArray::getObject)
                .filter(obj -> shardManager.getGuildById(obj.getLong("id")) != null)
                .filter(obj -> Permission.getPermissions(obj.getLong("permissions")).contains(Permission.ADMINISTRATOR) || obj.getBoolean("owner"))
                .map(obj -> shardManager.getGuildById(obj.getLong("id")))
                .collect(Collectors.toList());
        long guildId = Long.parseUnsignedLong(guildIdString);

        Guild guild = mutualGuilds.stream()
                .map(ISnowflake::getIdLong)
                .filter(id -> id.equals(guildId))
                .map(shardManager::getGuildById)
                .findFirst().orElse(null);
        if (guild == null)
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        GuildData guildData = GuildManager.getGuildData(guild);
        DataObject data = guildData.toData();
        DataObject returnObject = DataObject.empty()
                .put("guild", ObjectBuilder.serializeJDAGuild(guild))
                .put("http_code", guilds.getInt("http_code"))
                .put("data", data);
        return returnObject.toString();
    }
}
