package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenRoute implements Route
{
    @Override
    public Object handle(Request request, Response response) throws Exception
    {
        if (request.headers("authorization") == null)
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        String authHeader = request.headers("authorization");
        if (!authHeader.startsWith("AuthCode "))
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        String authCode = authHeader.substring(9);
        try
        {
            DataObject object = DataObject.empty();
            DataObject tokens = Bean.getInstance().getWebServer().retrieveTokens(authCode);
            if (tokens.getInt("status") > 304 || tokens.isNull("access_token"))
            {
                response.status(400);
                return DataObject.empty()
                        .put("code", tokens.isNull("code") ? 400 : tokens.getInt("code"))
                        .put("message", tokens.isNull("message") ? "Discord Error" : tokens.getString("message"))
                        .toString();
            }
            object.put("tokens", tokens);
            String accessToken = tokens.getString("access_token");
            DataObject userObject = WebServer.retrieveUser(accessToken);
            long id = userObject.getLong("id");
            int discriminator = Integer.parseInt(userObject.getString("discriminator"));
            String effectiveAvatarURL = "";
            if (userObject.isNull("avatar"))
                effectiveAvatarURL = "https://cdn.discordapp.com/embed/avatars/"+(discriminator % 5)+".png";
            else
            {
                String avatarHash = userObject.getString("avatar");
                boolean animated = avatarHash.startsWith("a_");
                effectiveAvatarURL = "https://cdn.discordapp.com/avatars/"+id+"/"+avatarHash+(animated ? ".gif" : ".png");
            }
            userObject.put("effective_avatar", effectiveAvatarURL);
            object.put("user", userObject);
            byte[] tokenBytes = Bean.getInstance().getAuthenticator().addSession(object);
            String token = new String(tokenBytes, StandardCharsets.UTF_8);
            response.header("authorization", "Token "+token);
            return userObject.toString();
        } catch (Exception ex)
        {
            response.status(500);
            return DataObject.empty()
                    .put("code", 500)
                    .put("message", "Internal Server Error")
                    .toString();
        }
    }
}
