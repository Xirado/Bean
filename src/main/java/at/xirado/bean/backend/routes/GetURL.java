package at.xirado.bean.backend.routes;

import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.http.client.utils.URIBuilder;
import spark.Request;
import spark.Response;

import java.net.URISyntaxException;

public class GetURL
{
    public static final String HOST = "discord.com";

    public static Object handle(Request request, Response response) throws URISyntaxException
    {
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost(HOST)
                .setPath("/api/oauth2/authorize")
                .addParameter("response_type", "code")
                .addParameter("client_id", WebServer.CLIENT_ID)
                .addParameter("scope", "identify guilds")
                .addParameter("permissions", "0")
                .addParameter("redirect_uri", WebServer.REDIRECT_URI)
                .addParameter("prompt", "consent");
        return DataObject.empty()
                .put("url", builder.build().toString())
                .toString();
    }
}
