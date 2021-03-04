package at.Xirado.Bean.Handlers;

import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Misc.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class MutedRoleManager
{

    private final ConcurrentHashMap<Long, Long> mutedRoles;

    public MutedRoleManager()
    {
        mutedRoles = new ConcurrentHashMap<>();
        init();
    }

    public void deleteMutedRole(long guildID)
    {
        mutedRoles.remove(guildID);
        String qry = "DELETE FROM mutedroles WHERE guildID = ?";
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
            Console.logger.error("Could not delete muted role!");
            return;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.execute();
            connection.close();
        }catch(Exception e)
        {
            Console.logger.error("Could not delete muted role!", e);
            return;
        }
    }

    public void setMutedRole(long guildID, long roleID)
    {
        if(mutedRoles.containsKey(guildID))
        {
            mutedRoles.remove(guildID);
            mutedRoles.put(guildID, roleID);
        }
        String qry = "INSERT INTO mutedroles (guildID, roleID) values (?,?) ON DUPLICATE KEY UPDATE roleID = ?";
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
            Console.logger.error("Could not set muted role!");
            return;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, roleID);
            ps.setLong(3, roleID);
            ps.execute();
            connection.close();
        }catch(Exception e)
        {
            Console.logger.error("Could not set muted role!", e);
            return;
        }
    }

    public Long getMutedRole(long guildID)
    {
        if(mutedRoles.containsKey(guildID))
        {
            return mutedRoles.get(guildID);
        }
        String qry = "SELECT roleID FROM mutedroles WHERE guildID = ?";
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
            Console.logger.error("Could not initialize MutedRole Table!");
            return null;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ResultSet rs = ps.executeQuery();
            connection.close();
            if(rs.next())
            {
                long role = rs.getLong("roleID");
                mutedRoles.remove(guildID);
                mutedRoles.put(guildID, role);
                return role;
            }
            return null;

        }catch(Exception e)
        {
            Console.logger.error("Could not initialize Muted Role Table!", e);
            return null;
        }
    }

    private void init()
    {
        String qry = "CREATE TABLE IF NOT EXISTS mutedroles (guildID BIGINT PRIMARY KEY, roleID BIGINT)";
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
            Console.logger.error("Could not initialize MutedRole Table!");
            return;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.execute();
            connection.close();
        }catch(Exception e)
        {
            Console.logger.error("Could not initialize Muted Role Table!", e);
            return;
        }
    }


}
