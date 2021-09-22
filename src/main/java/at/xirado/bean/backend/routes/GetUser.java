package at.xirado.bean.backend.routes;

import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;

import java.io.IOException;

public class GetUser
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
        long id = user.getObject("user").getLong("id");
        int discriminator = Integer.parseInt(user.getObject("user").getString("discriminator"));
        String effectiveAvatarURL = "";
        if (user.getObject("user").isNull("avatar"))
            effectiveAvatarURL = "https://cdn.discordapp.com/embed/avatars/"+(discriminator % 5)+".png";
        else
        {
            String avatarHash = user.getObject("user").getString("avatar");
            boolean animated = avatarHash.startsWith("a_");
            effectiveAvatarURL = "https://cdn.discordapp.com/avatars/"+id+"/"+avatarHash+(animated ? ".gif" : ".png");
        }
        user.getObject("user").put("effective_avatar", effectiveAvatarURL);
        return user.toString();
    }
}
