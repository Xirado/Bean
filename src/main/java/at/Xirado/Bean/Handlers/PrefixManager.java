package at.Xirado.Bean.Handlers;

import at.Xirado.Bean.Misc.SQL;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class PrefixManager
{
    public ConcurrentHashMap<Long, String> prefix;

    public PrefixManager()
    {
        this.prefix = new ConcurrentHashMap<>();
    }

    public void setPrefix(@NotNull long guildid, @NotNull String newPrefix)
    {
        prefix.put(guildid, newPrefix);
        try
        {
            Connection connection = SQL.getConnectionFromPool();
            String qry = "INSERT INTO commandPrefixes (guildID,prefix) values (?,?) ON DUPLICATE KEY UPDATE prefix = ?";
            PreparedStatement ps = connection.prepareStatement(qry);
            ps.setLong(1, guildid);
            ps.setString(2, newPrefix);
            ps.setString(3, newPrefix);
            ps.execute();
            connection.close();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }

    }
    public String getPrefix(long guildid)
    {
        String prefix = null;

        if(this.prefix.containsKey(guildid))
        {
            prefix = this.prefix.get(guildid);
        }
        else
        {
            try {
                Connection connection = SQL.getConnectionFromPool();
                PreparedStatement ps = connection.prepareStatement("SELECT prefix FROM commandPrefixes WHERE guildID = ?");
                ps.setLong(1, guildid);
                ResultSet rs = ps.executeQuery();
                connection.close();
                if(rs.next())
                {
                    prefix = rs.getString("prefix");
                    this.prefix.put(guildid, prefix);
                }else
                {
                    this.setPrefix(guildid, "+");
                    return "+";
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        return prefix;
    }
}
