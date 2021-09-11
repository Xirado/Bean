package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GetGuilds
{

    public static Object handle(Request request, Response response) throws IOException
    {
        if (request.headers("access_token") == null)
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        DataObject user;
        try
        {
            user = WebServer.handleCache(Long.parseLong(request.headers("expires_on")), request.headers("access_token"), request.headers("refresh_token"));
        }catch (IOException ex)
        {
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Bad request")
                    .toString();

        }
        String accessToken = user.getString("access_token");
        DataArray guilds = WebServer.retrieveGuilds(accessToken);
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        List<Guild> mutualGuilds = guilds.stream(DataArray::getObject)
                .filter(obj -> jda.getGuildById(obj.getLong("id")) != null)
                .filter(obj -> Permission.getPermissions(obj.getLong("permissions")).contains(Permission.ADMINISTRATOR) || obj.getBoolean("owner"))
                .map(obj -> jda.getGuildById(obj.getLong("id")))
                .collect(Collectors.toList());
        DataArray mutualGuildsArray = DataArray.empty();
        for(Guild guild : mutualGuilds)
        {
            DataObject object = DataObject.empty();
            object.put("name", guild.getName());
            object.put("id", guild.getId());
            object.put("icon", guild.getIconUrl());
            mutualGuildsArray.add(object);
        }
        return mutualGuildsArray.toString();
    }
}
