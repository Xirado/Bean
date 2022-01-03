package at.xirado.bean.backend;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import java.io.IOException;

public class DiscordCredentials
{
    private String accessToken;
    private String refreshToken;
    private String[] scopes;
    private String tokenType;
    private long expiresIn;
    
    public DiscordCredentials(DataObject object)
    {
        Checks.notNull(object, "Object");
        if (object.isNull("access_token") || object.isNull("refresh_token") || object.isNull("scope") || object.isNull("token_type"))
            throw new IllegalArgumentException("Object is not a valid Discord Token response!");
        this.accessToken = object.getString("access_token");
        this.refreshToken = object.getString("refresh_token");
        this.scopes = object.getString("scope").split("\\s+");
        this.tokenType = object.getString("token_type");
        this.expiresIn = object.getLong("expires_in");
    }

    public DiscordCredentials refreshToken() throws IOException
    {
        DataObject object = Bean.getInstance().getWebServer().refreshToken(this.refreshToken);
        this.accessToken = object.getString("access_token");
        this.refreshToken = object.getString("refresh_token");
        this.scopes = object.getString("scope").split("\\s+");
        this.tokenType = object.getString("token_type");
        this.expiresIn = object.getLong("expires_in");
        return this;
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public long getExpiresIn()
    {
        return expiresIn;
    }

    public String getTokenType()
    {
        return tokenType;
    }

    public String[] getScopes()
    {
        return scopes;
    }
}
