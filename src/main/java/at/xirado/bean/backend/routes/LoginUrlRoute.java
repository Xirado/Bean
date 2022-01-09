package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.http.client.utils.URIBuilder;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URISyntaxException;

public class LoginUrlRoute implements Route
{
    public static final String HOST = "discord.com";

    @Override
    public Object handle(Request request, Response response) throws Exception
    {
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost(HOST)
                .setPath("/api/oauth2/authorize")
                .addParameter("response_type", "code")
                .addParameter("client_id", Bean.getInstance().getWebServer().getClientId())
                .addParameter("scope", "identify guilds")
                .addParameter("permissions", "0")
                .addParameter("redirect_uri", Bean.getInstance().getWebServer().getRedirectUri())
                .addParameter("prompt", "consent");
        return DataObject.empty()
                .put("url", builder.build().toString())
                .toString();
    }
}
