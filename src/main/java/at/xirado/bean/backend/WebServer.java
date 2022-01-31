package at.xirado.bean.backend;


import at.xirado.bean.Bean;
import at.xirado.bean.backend.routes.*;
import at.xirado.bean.misc.FrequencyCounter;
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class WebServer
{
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public static final String BASE_URL = "https://discord.com/api/v9";

    public static final Map<String, DataObject> USER_CACHE = new ConcurrentHashMap<>();

    public WebServer(int port)
    {
        DataObject config = Bean.getInstance().getConfig();
        if (config.anyNull("client_id", "client_secret", "redirect_uri"))
            throw new IllegalStateException("Missing Discord Oauth2 configuration!");
        clientId = config.getString("client_id");
        clientSecret = config.getString("client_secret");
        redirectUri = config.getString("redirect_uri");
        ipAddress("127.0.0.1");
        port(port);
        enableCORS("*", "*", "*");
        before(((request, response) -> {
            switch (request.raw().getMethod()) {
                case "GET" -> Metrics.REQUESTS.labels("get").inc();
                case "POST" -> Metrics.REQUESTS.labels("post").inc();
                case "PUT" -> Metrics.REQUESTS.labels("put").inc();
                case "DELETE" -> Metrics.REQUESTS.labels("delete").inc();
                case "PATCH" -> Metrics.REQUESTS.labels("patch").inc();
                default -> Metrics.REQUESTS.labels("other").inc();
            }
        }));
        get("/guilds", new GuildsRoute());
        get("/token", new TokenRoute());
        get("/login", new LoginUrlRoute());
        get("/guild", new GuildPageRoute());
        get("/invite", new InviteURLRoute());
        get("/user", new UserRoute());
        post("/modifyguild", new GuildDataRoute());
        get("/commands", new CommandsRoute());
        get("/guilds/:guild/levels", new LeaderboardRoute());
        get("/*", (req, res) -> {
            res.status(404);
            return DataObject.empty()
                    .put("code", 404)
                    .put("message", "Site not found")
                    .toString();
        });
    }

    public DataObject refreshToken(String refreshToken) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        DataObject requestObject = DataObject.empty()
                .put("client_id", clientId)
                .put("client_secret", clientSecret)
                .put("grant_type", "refresh_token")
                .put("refresh_token", refreshToken);

        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), requestObject.toJson());

        Request request = new Request.Builder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .url(BASE_URL + "/oauth2/token")
                .build();

        Call call = client.newCall(request);

        Response response = call.execute();
        return DataObject.fromJson(response.body().byteStream());
    }

    public static DataObject retrieveUser(String accessToken) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .header("authorization", "Bearer " + accessToken)
                .get()
                .url(BASE_URL + "/users/@me")
                .build();

        Call call = client.newCall(request);

        Response response = call.execute();
        return DataObject.fromJson(response.body().byteStream());
    }

    public static DataObject retrieveGuilds(String accessToken) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "Bean (https://bean.bz, " + Bean.getBeanVersion() + ")")
                .get()
                .url(BASE_URL + "/users/@me/guilds")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String body = response.body().string();
        DataObject object = DataObject.empty();
        if (response.isSuccessful()) // Below 400 => Returned an array of guilds
        {
            return object.put("guilds", DataArray.fromJson(body))
                    .put("http_code", response.code());
        }
        DataObject responseBody = DataObject.fromJson(body);
        if (!responseBody.isNull("message"))
            object.put("message", responseBody.getString("message"));
        if (!responseBody.isNull("code"))
            object.put("code", responseBody.getInt("code"));
        object.put("http_code", response.code());
        return object;
    }

    private static void enableCORS(final String origin, final String methods, final String headers)
    {

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null)
            {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null)
            {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", "authorization, *, Authorization");
            response.header("Access-Control-Expose-Headers", "authorization, *, Authorization");
            response.header("Access-Control-Allow-Headers", headers);
            response.type("application/json");
        });
    }

    public DataObject retrieveTokens(String code) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build();
        Request request = new Request.Builder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .url(BASE_URL + "/oauth2/token")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        return DataObject.fromJson(response.body().byteStream()).put("status", response.code());
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getClientSecret()
    {
        return clientSecret;
    }

    public String getRedirectUri()
    {
        return redirectUri;
    }
}
