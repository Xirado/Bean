package at.xirado.bean.data;

import at.xirado.bean.Bean;
import at.xirado.bean.data.database.SQLBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GuildManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);

    private static final Map<Long, GuildData> GUILD_DATA = ExpiringMap.builder()
            .expiration(30, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expirationListener((k, v) -> LOGGER.debug("Unloaded GuildData of guild " + k))
            .build();

    public static GuildData getGuildData(Guild guild) {
        if (GUILD_DATA.containsKey(guild.getIdLong()))
            return GUILD_DATA.get(guild.getIdLong());
        GuildData retrievedData = retrieveGuildData(guild.getIdLong());
        if (retrievedData != null) {
            GUILD_DATA.put(guild.getIdLong(), retrievedData);
            return retrievedData;
        }
        GuildData createdData = createGuildData(guild);
        GUILD_DATA.put(guild.getIdLong(), createdData);
        return createdData;
    }

    public static Optional<GuildData> optGuildData(long guildId) {
        if (GUILD_DATA.containsKey(guildId))
            return Optional.of(GUILD_DATA.get(guildId));
        GuildData retrievedData = retrieveGuildData(guildId);
        return Optional.ofNullable(retrievedData);
    }

    public static String getGuildDataJSON(long guildId) throws JsonProcessingException {
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
        if (guild != null)
            return getGuildData(guild).toPrettyString();
        GuildData retrievedData = retrieveGuildData(guildId);
        if (retrievedData != null)
            return retrievedData.toPrettyString();
        return DataObject.empty().toPrettyString();
    }

    private static GuildData retrieveGuildData(long guildID) {
        String sql = "SELECT data FROM guild_settings WHERE guildID = ?";
        var query = new SQLBuilder(sql)
                .addParameter(guildID);
        try (var rs = query.executeQuery()) {
            if (rs.next()) return new GuildData(guildID, DataObject.fromJson(rs.getString("data")));
            return null;
        } catch (SQLException ex) {
            LOGGER.error("Could not retrieve guild data!", ex);
            return null;
        }
    }

    private static void updateGuildData(long guildID, DataObject data) {
        String sql = "INSERT INTO guild_settings (guildID, data) values (?,?) ON DUPLICATE KEY UPDATE data = ?";
        try {
            String jsonString = data.toString();
            var query = new SQLBuilder(sql)
                    .addParameters(guildID, jsonString, jsonString);
            query.execute();
        } catch (SQLException exception) {
            LOGGER.error("Could not update guild data!", exception);
        }
    }

    private static GuildData createGuildData(Guild guild) {
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
