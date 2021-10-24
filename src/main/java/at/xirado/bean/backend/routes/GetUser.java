package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.Authenticator;
import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GetUser
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
        DataObject user = authenticator.getData(tokenBytes);
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
