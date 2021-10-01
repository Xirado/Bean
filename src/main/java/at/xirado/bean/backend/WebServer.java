package at.xirado.bean.backend;


import at.xirado.bean.Bean;
import at.xirado.bean.backend.routes.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;

public class WebServer
{
    public static final String CLIENT_ID = Bean.getInstance().getConfig().getString("client_id");
    public static final String CLIENT_SECRET = Bean.getInstance().getConfig().getString("client_secret");
    public static final String REDIRECT_URI = Bean.getInstance().getConfig().getString("redirect_uri");
    public static final String BASE_URL = "https://discord.com/api/v9";

    public static final Map<String, DataObject> USER_CACHE = new ConcurrentHashMap<>();

    public WebServer(int port)
    {
        port(port);
        if (!Bean.getInstance().isDebug())
        {
            secure("cert.jks", Bean.getInstance().getConfig().getString("cert_pw"), null, null);

        }
        enableCORS("*", "*", "*");
        get("/guilds", GetGuilds::handle);
        get("/token", GetTokens::handle);
        get("/login", GetURL::handle);
        get("/guild", GetGuildPage::handle);
        get("/invite", GetInviteURL::handle);
        get("/user", GetUser::handle);
        post("/modifyguild", SetGuildData::handle);
    }

    public static DataObject refreshToken(String refreshToken) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        DataObject requestObject = DataObject.empty()
                .put("client_id", CLIENT_ID)
                .put("client_secret", CLIENT_SECRET)
                .put("grant_type", "refresh_token")
                .put("refresh_token", refreshToken);

        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), requestObject.toJson());

        Request request = new Request.Builder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .url(BASE_URL+"/oauth2/token")
                .build();

        Call call = client.newCall(request);

        Response response = call.execute();
        return DataObject.fromJson(response.body().byteStream());
    }

    public static DataObject handleCache(long expiresOn, String accessToken, String refreshToken) throws IOException
    {
        boolean updateToken = false;
        String oldToken = accessToken;
        DataObject returnObject = DataObject.empty();
        if (expiresOn < System.currentTimeMillis()/1000)
        {
            DataObject newTokens = refreshToken(refreshToken);
            accessToken = newTokens.getString("access_token");
            refreshToken = newTokens.getString("refresh_token");
            expiresOn = (System.currentTimeMillis()/1000) + newTokens.getLong("expires_in");
            updateToken = true;
            returnObject.put("access_token", accessToken)
                    .put("refresh_token", refreshToken)
                    .put("expires_on", expiresOn);
        }
        if (!updateToken)
        {
            returnObject.put("access_token", accessToken)
                    .put("refresh_token", refreshToken)
                    .put("expires_on", expiresOn);
        }
        WebServer webServer = Bean.getInstance().getWebServer();
        Map<String, DataObject> userCache = webServer.USER_CACHE;
        DataObject oldUserCache = userCache.get(oldToken);
        userCache.remove(oldToken);
        if (oldUserCache != null)
        {
            userCache.put(accessToken, oldUserCache);
            long cached = oldUserCache.getLong("cached");
            if (System.currentTimeMillis() < cached+300)
                returnObject.put("user", oldUserCache);
            else
                returnObject.put("user", retrieveUser(accessToken));

        }
        else
        {
            DataObject newUser = WebServer.retrieveUser(accessToken);
            newUser.put("cached", System.currentTimeMillis()/1000);
            userCache.put(accessToken, newUser);
            returnObject.put("user", newUser);
        }
        return returnObject;
    }


    public static DataObject retrieveUser(String accessToken) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        Request request = new Request.Builder()
                .header("authorization", "Bearer "+accessToken)
                .get()
                .url(BASE_URL+"/users/@me")
                .build();

        Call call = client.newCall(request);

        Response response = call.execute();
        return DataObject.fromJson(response.body().byteStream());
    }

    public static DataObject retrieveGuilds(String accessToken) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer "+accessToken)
                .header("User-Agent", "Bean (https://bean.bz, "+Bean.getBeanVersion()+")")
                .get()
                .url(BASE_URL+"/users/@me/guilds")
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
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            response.type("application/json");
        });
    }

    public static DataObject retrieveTokens(String code) throws IOException
    {
        OkHttpClient client = Bean.getInstance().getOkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .build();
        Request request = new Request.Builder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .url(BASE_URL+"/oauth2/token")
                .build();

        Call call = client.newCall(request);

        Response response = call.execute();
        return DataObject.fromJson(response.body().byteStream());
    }

}
