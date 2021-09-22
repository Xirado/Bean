package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.WebServer;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;

import java.io.IOException;

public class SetGuildData
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
        long expiresOn = Long.parseLong(expiresString);
        DataObject user = WebServer.handleCache(expiresOn, accessToken, refreshToken);
        DataObject body = DataObject.fromJson(request.bodyAsBytes());
        if (body.isNull("guild") || body.isNull("method") || body.isNull("parameters"))
        {
            response.status(400);
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Bad request")
                    .toString();
        }
        DataObject parameters = body.getObject("parameters");
        long guildId = Long.parseUnsignedLong(body.getString("guild"));
        long userId = user.getObject("user").getLong("id");
        String method = body.getString("method");
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
        {
            response.status(400);
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Bad request")
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
        if (method.equals("log_channel"))
        {
            if (parameters.isNull("log_channel"))
            {
                guildData.put("log_channel", null);
                return user.toString();
            }
            long newChannelId = Long.parseUnsignedLong(parameters.getString("log_channel"));
            guildData.put("log_channel", newChannelId).update();
            TextChannel channel = guild.getTextChannelById(newChannelId);
            channel.sendMessage("This is now the new channel used for logging!").queue();
        }
        return user.toString();
    }
}
