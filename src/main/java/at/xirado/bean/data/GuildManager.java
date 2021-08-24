package at.xirado.bean.data;

import at.xirado.bean.data.database.Database;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);

    private static final Map<Long, GuildData> GUILD_DATA = new ConcurrentHashMap<>();

    public static GuildData getGuildData(Guild guild)
    {
        if (GUILD_DATA.containsKey(guild.getIdLong()))
            return GUILD_DATA.get(guild.getIdLong());
        GuildData retrievedData = retrieveGuildData(guild.getIdLong());
        if (retrievedData != null)
        {
            GUILD_DATA.put(guild.getIdLong(), retrievedData);
            return retrievedData;
        }
        GuildData createdData = createGuildData(guild);
        GUILD_DATA.put(guild.getIdLong(), createdData);
        return createdData;
    }

    public static String getGuildDataJSON(long guildID) throws JsonProcessingException
    {
        if (!GUILD_DATA.containsKey(guildID)) return null;
        return GUILD_DATA.get(guildID).toPrettyString();
    }

    private static GuildData retrieveGuildData(long guildID)
    {
        String sql = "SELECT data FROM guildSettings WHERE guildID = ?";
        var query = new SQLBuilder(sql)
                .addParameter(guildID);
        try(var rs = query.executeQuery())
        {
            if (rs.next()) return new GuildData(guildID, DataObject.parse(rs.getString("data")));
            return null;
        }catch (SQLException ex)
        {
            LOGGER.error("Could not retrieve guild data!", ex);
            return null;
        }
    }

    private static void updateGuildData(long guildID, DataObject data)
    {
        String sql = "INSERT INTO guildSettings (guildID, data) values (?,?) ON DUPLICATE KEY UPDATE data = ?";
        try
        {
            String jsonString = data.toJson();
            var query = new SQLBuilder(sql)
                    .addParameters(guildID, jsonString, jsonString);
            query.execute();
        } catch (SQLException | JsonProcessingException exception)
        {
            LOGGER.error("Could not update guild data!", exception);
        }
    }

    private static GuildData createGuildData(Guild guild)
    {
        DataObject json = DataObject.empty()
                .put("id", guild.getIdLong())
                .put("name", guild.getName())
                .put("owner", guild.getOwnerIdLong())
                .put("created", guild.getTimeCreated().toString())
                .put("joined", guild.getSelfMember().getTimeJoined().toString())
                .put("command_prefix", "+")
                .put("vip", false)
                .put("language", guild.getLocale().toString());
        updateGuildData(guild.getIdLong(), json);
        return new GuildData(guild.getIdLong(), json);
    }
}
