package at.xirado.bean.backend.routes;

import at.xirado.bean.backend.WebServer;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;

import java.io.IOException;

public class GetTokens
{
    public static Object handle(Request request, Response response) throws IOException
    {
        if (request.headers("code") == null)
        {
            response.status(401);
            return DataObject.empty()
                    .put("code", 401)
                    .put("message", "Unauthorized")
                    .toString();
        }
        String code = request.headers("code");
        DataObject tokens = WebServer.retrieveTokens(code);
        System.out.println("Sending tokens:\n"+tokens.toPrettyString());
        return tokens.toString();
    }
}
