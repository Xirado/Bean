package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.ObjectBuilder;
import at.xirado.bean.backend.WebServer;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GetGuildPage
{
    public static Object handle(Request request, Response response) throws IOException
    {
        String accessToken = request.headers("access_token");
        String refreshToken = request.headers("refresh_token");
        String expiresString = request.headers("expires_on");
        if (accessToken == null || refreshToken == null || expiresString == null)
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
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        List<Guild> mutualGuilds = guilds.getArray("guilds").stream(DataArray::getObject)
                .filter(obj -> jda.getGuildById(obj.getLong("id")) != null)
                .filter(obj -> Permission.getPermissions(obj.getLong("permissions")).contains(Permission.ADMINISTRATOR) || obj.getBoolean("owner"))
                .map(obj -> jda.getGuildById(obj.getLong("id")))
                .collect(Collectors.toList());
        long guildId = Long.parseUnsignedLong(guildIdString);

        Guild guild = mutualGuilds.stream()
                .map(ISnowflake::getIdLong)
                .filter(id -> id.equals(guildId))
                .map(jda::getGuildById)
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
                .put("guild", ObjectBuilder.serializeGuild(guild))
                .put("http_code", guilds.getInt("http_code"))
                .put("data", data);
        return returnObject.toString();
    }
}
