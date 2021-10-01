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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class SetGuildData
{

    public static final Set<String> ALLOWED_SETTINGS = Set.of("log_channel", "dj_roles");

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
        long expiresOn = Long.parseLong(expiresString);
        DataObject user = WebServer.handleCache(expiresOn, accessToken, refreshToken);
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
        long userId = user.getObject("user").getLong("id");
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
        {
            response.status(400);
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Invalid guild")
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
                    .put("message", "Unauthorized (You are missing ADMINISTRATOR privileges!)")
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
