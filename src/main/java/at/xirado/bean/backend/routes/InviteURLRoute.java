package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.http.client.utils.URIBuilder;
import spark.Request;
import spark.Response;
import spark.Route;

public class InviteURLRoute implements Route
{
    public static final String HOST = "discord.com";

    @Override
    public Object handle(Request request, Response response) throws Exception
    {
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost(HOST)
                .setPath("/oauth2/authorize")
                .addParameter("client_id", Bean.getInstance().getWebServer().getClientId())
                .addParameter("scope", "bot applications.commands")
                .addParameter("permissions", "275191770223");
        return DataObject.empty()
                .put("url", builder.build().toString())
                .toString();
    }

    public static String getInviteURL()
    {
        try
        {
            URIBuilder builder = new URIBuilder()
                    .setScheme("https")
                    .setHost(HOST)
                    .setPath("/oauth2/authorize")
                    .addParameter("client_id", Bean.getInstance().getWebServer().getClientId())
                    .addParameter("scope", "bot applications.commands")
                    .addParameter("permissions", "275191770223");
            return builder.build().toString();
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
