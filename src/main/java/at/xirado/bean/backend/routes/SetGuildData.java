package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.WebServer;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import org.apache.commons.codec.binary.Hex;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public class SetGuildData
{

    public static final Set<String> ALLOWED_SETTINGS = Set.of("log_channel", "dj_roles", "allow_earrape");

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
        }catch (ErrorResponseException ex)
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
