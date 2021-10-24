package at.xirado.bean.backend;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Authenticator
{
    private final Map<ByteBuffer, DataObject> users = new HashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public Authenticator()
    {
        Bean.getInstance().getExecutor().scheduleAtFixedRate(() -> {
            for (Map.Entry<ByteBuffer, DataObject> entries : users.entrySet())
            {
                ByteBuffer token = entries.getKey();
                DataObject user = entries.getValue();
                long generationTime = user.getLong("generation_time");
                OffsetDateTime maxAge = OffsetDateTime
                        .of(LocalDateTime.ofEpochSecond(generationTime, 0, ZoneOffset.UTC), ZoneOffset.UTC)
                        .plusDays(3);
                if (maxAge.isBefore(OffsetDateTime.now()))
                    invalidate(token);
            }
        }, 1, 1, TimeUnit.DAYS);
    }

    public byte[] generateNewToken()
    {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encode(randomBytes);
    }

    public byte[] addSession(DataObject object)
    {
        object.put("generation_time", System.currentTimeMillis()/1000);
        object.put("discord_timestamp", System.currentTimeMillis()/1000);
        byte[] token = generateNewToken();
        users.put(ByteBuffer.wrap(token), object);
        return token;
    }

    public void invalidate(byte[] token)
    {
        users.remove(ByteBuffer.wrap(token));
    }

    public void invalidate(ByteBuffer buffer)
    {
        users.remove(buffer);
    }

    public boolean isAuthenticated(byte[] token)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(token);
        if (!users.containsKey(byteBuffer))
            return false;
        DataObject object = users.get(byteBuffer);
        long generationTime = object.getLong("generation_time");
        OffsetDateTime maxAge = OffsetDateTime
                .of(LocalDateTime.ofEpochSecond(generationTime, 0, ZoneOffset.UTC), ZoneOffset.UTC)
                .plusDays(3);
        if (maxAge.isBefore(OffsetDateTime.now()))
        {
            invalidate(byteBuffer);
            return false;
        }
        return true;
    }

    public DataObject getUser(byte[] token)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(token);
        if (!users.containsKey(byteBuffer))
            return null;

        return users.get(byteBuffer).getObject("user");
    }

    public DataObject getData(byte[] token)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(token);
        if (!users.containsKey(byteBuffer))
            return null;

        return users.get(byteBuffer);
    }

    public DiscordCredentials getCredentials(byte[] token)
    {
        DataObject data = getData(token);
        if (data == null)
            return null;
        return new DiscordCredentials(data.getObject("tokens"));
    }

    public boolean isAccessTokenExpired(byte[] token)
    {
        if (!isAuthenticated(token))
            throw new IllegalStateException("User unauthenticated!");
        DataObject data = getData(token);
        return data.getLong("discord_timestamp") + data.getObject("tokens").getLong("expires_in") < System.currentTimeMillis()/1000;
    }

    public void refreshAccessToken(byte[] token) throws IOException
    {
        if (!isAuthenticated(token))
            throw new IllegalStateException("User unauthenticated!");
        DiscordCredentials credentials = getCredentials(token);
        if (!isAccessTokenExpired((token)))
            return;
        DiscordCredentials newCredentials = credentials.refreshToken();
        DataObject tokens = DataObject.empty()
                .put("access_token", newCredentials.getAccessToken())
                .put("refresh_token", newCredentials.getRefreshToken())
                .put("scope", String.join(" ", newCredentials.getScopes()))
                .put("token_type", newCredentials.getTokenType())
                .put("expires_in", newCredentials.getExpiresIn());
        DataObject data = getData(token);
        data.put("discord_timestamp", System.currentTimeMillis()/1000)
                .put("tokens", tokens);
        users.put(ByteBuffer.wrap(token), data);
    }
}
