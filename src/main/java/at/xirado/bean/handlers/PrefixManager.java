package at.xirado.bean.handlers;

import at.xirado.bean.misc.SQL;
import at.xirado.bean.misc.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class PrefixManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PrefixManager.class);

    private final ConcurrentHashMap<Long, String> prefixes;

    public PrefixManager()
    {
        this.prefixes = new ConcurrentHashMap<>();
    }

    public void setPrefix(long guildid, @NotNull String newPrefix)
    {
        prefixes.put(guildid, newPrefix);
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
           LOGGER.error("Could not set Prefix!", new Exception());
           return;
        }
        String qry = "INSERT INTO commandPrefixes (guildID,prefix) values (?,?) ON DUPLICATE KEY UPDATE prefix = ?";
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildid);
            ps.setString(2, newPrefix);
            ps.setString(3, newPrefix);
            ps.execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not set Prefix!", ex);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public String getPrefix(long guildid)
    {
        if(prefixes.containsKey(guildid)) return prefixes.get(guildid);

        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
            LOGGER.error("Could not get Prefix!", new Exception());
            return null;
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT prefix FROM commandPrefixes WHERE guildID = ?"))
        {
            ps.setLong(1, guildid);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                String prefix = rs.getString("prefix");
                this.prefixes.put(guildid, prefix);
                return prefix;
            }else
            {
                this.setPrefix(guildid, "+");
                return "+";
            }
        } catch (SQLException ex) {
            LOGGER.error("Could not get Prefix!", ex);
            return "+";
        } finally
        {
            Util.closeQuietly(connection);
        }
    }
}
