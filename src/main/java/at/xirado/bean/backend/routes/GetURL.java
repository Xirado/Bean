package at.xirado.bean.backend.routes;

import at.xirado.bean.backend.WebServer;
import com.jagrosh.jdautilities.oauth2.requests.OAuth2URL;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.http.client.utils.URIBuilder;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URISyntaxException;

public class GetURL
{
    public static final String HOST = "discord.com";
    public static final String URL = "https:///api/oauth2/authorize" +
            "?response_type=code" +
            "&client_id=%s" +
            "&scope=identify%20guilds" +
            "&redirect_uri=%s" +
            "&prompt=consent";

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
