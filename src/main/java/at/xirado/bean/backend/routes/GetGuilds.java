package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.Authenticator;
import at.xirado.bean.backend.DiscordCredentials;
import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.commons.codec.binary.Hex;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class GetGuilds
{

    public static Object handle(Request request, Response response) throws IOException
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
        DataObject object = authenticator.getData(tokenBytes);
        DiscordCredentials credentials = authenticator.getCredentials(tokenBytes);

        String accessToken = credentials.getAccessToken();
        DataObject guilds = WebServer.retrieveGuilds(accessToken);
        if (guilds.isNull("guilds"))
        {
            DataObject o = DataObject.empty();
            o.put("http_code", guilds.getInt("http_code"));
            if (!guilds.isNull("code"))
                o.put("code", guilds.getInt("code"));
            if (!guilds.isNull("message"))
                o.put("message", guilds.getString("message"));
            return o.toString();
        }
        ShardManager shardManager = Bean.getInstance().getShardManager();
        List<Guild> mutualGuilds = guilds.getArray("guilds").stream(DataArray::getObject)
                .filter(obj -> shardManager.getGuildById(obj.getLong("id")) != null)
                .filter(obj -> Permission.getPermissions(obj.getLong("permissions")).contains(Permission.ADMINISTRATOR) || obj.getBoolean("owner"))
                .map(obj -> shardManager.getGuildById(obj.getLong("id")))
                .collect(Collectors.toList());
        DataArray mutualGuildsArray = DataArray.empty();
        for(Guild guild : mutualGuilds)
        {
            DataObject o = DataObject.empty();
            o.put("name", guild.getName());
            o.put("id", guild.getId());
            o.put("icon", guild.getIconUrl());
            StringBuilder initials = new StringBuilder();
            for (String s : guild.getName().split("\\s+")) {
                initials.append(s.charAt(0));
            }
            o.put("initials", initials.toString());
            mutualGuildsArray.add(o);
        }
        return DataObject.empty()
                .put("guilds", mutualGuildsArray)
                .put("http_code", guilds.getInt("http_code"))
                .toString();
    }
}
