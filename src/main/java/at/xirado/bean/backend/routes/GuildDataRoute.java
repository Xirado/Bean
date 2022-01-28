package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class GuildDataRoute implements Route
{

    public static final Set<String> ALLOWED_SETTINGS = Set.of("log_channel", "dj_roles", "allow_earrape", "no_xp_channels");

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
        String token = authHeader.substring(7);
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        if (!Bean.getInstance().getAuthenticator().isAuthenticated(tokenBytes))
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Invalid token (Try logging out and in again)")
                    .toString();
        }
        DataObject user = Bean.getInstance().getAuthenticator().getUser(tokenBytes);
        DataObject body = DataObject.fromJson(request.bodyAsBytes());
        if (body.isNull("guild") || body.isNull("data"))
        {
            response.status(400);
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Bad request")
                    .toString();
        }
        long guildId = Long.parseUnsignedLong(body.getString("guild"));
        long userId = user.getLong("id");
        ShardManager manager = Bean.getInstance().getShardManager();
        Guild guild = manager.getGuildById(guildId);
        if (guild == null)
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        Member member;
        try
        {
            member = guild.retrieveMemberById(userId).complete();
        } catch (ErrorResponseException ex)
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", ex.getMessage())
                    .toString();
        }
        if (!member.hasPermission(Permission.ADMINISTRATOR))
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        GuildData guildData = GuildManager.getGuildData(guild);

        DataObject updatedData = body.getObject("data"); // Stuff to update
        DataObject currentData = guildData.toData(); // old data

        for (String key : updatedData.keys())
        {
            if (ALLOWED_SETTINGS.contains(key))
                currentData.put(key, updatedData.get(key));
        }
        guildData.update();
        return user.toString();
    }
}
