package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.Authenticator;
import at.xirado.bean.backend.DiscordCredentials;
import at.xirado.bean.backend.DiscordUtils;
import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuildsRoute implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String authHeader = request.headers("authorization");
        if (authHeader == null || !authHeader.startsWith("Token ")) {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        String token = authHeader.substring(7);
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        Authenticator authenticator = Bean.getInstance().getAuthenticator();
        if (!authenticator.isAuthenticated(tokenBytes)) {
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
        if (guilds.isNull("guilds")) {
            DataObject o = DataObject.empty();
            o.put("http_code", guilds.getInt("http_code"));
            if (!guilds.isNull("code"))
                o.put("code", guilds.getInt("code"));
            if (!guilds.isNull("message"))
                o.put("message", guilds.getString("message"));
            return o.toString();
        }
        ShardManager shardManager = Bean.getInstance().getShardManager();
        List<DataObject> adminGuilds = guilds.getArray("guilds").stream(DataArray::getObject)
                .filter(obj -> Permission.getPermissions(obj.getLong("permissions")).contains(Permission.ADMINISTRATOR) || obj.getBoolean("owner"))
                .collect(Collectors.toList());
        DataArray guildArray = DataArray.empty();
        List<DataObject> nonInvitedGuilds = new ArrayList<>();
        for (DataObject guild : adminGuilds) {
            String name = guild.getString("name");
            long id = guild.getLong("id");
            String iconHash = guild.isNull("icon") ? null : guild.getString("icon");
            boolean botJoined = shardManager.getGuildById(id) != null;
            DataObject o = DataObject.empty();
            o.put("name", name);
            o.put("id", id + "");
            if (iconHash != null)
                o.put("icon", DiscordUtils.getIconUrl(id + "", iconHash));
            o.put("bot_joined", botJoined);
            StringBuilder initials = new StringBuilder();
            for (String s : name.split("\\s+")) {
                initials.append(s.charAt(0));
            }
            o.put("initials", initials.toString());
            if (botJoined)
                guildArray.add(o);
            else
                nonInvitedGuilds.add(o);
        }
        guildArray.addAll(nonInvitedGuilds);
        return DataObject.empty()
                .put("guilds", guildArray)
                .put("base_invite_url", InviteURLRoute.getInviteURL())
                .put("http_code", guilds.getInt("http_code"))
                .toString();
    }
}
