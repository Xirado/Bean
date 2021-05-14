package at.xirado.bean.handlers;

import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

public class MutedRoleManager
{

    private static final Logger logger = LoggerFactory.getLogger(MutedRoleManager.class);

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
        Connection connection = Database.getConnectionFromPool();
        if(connection == null)
        {
            logger.error("Could not delete muted role!", new Exception());
            return;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.execute();
        }catch(Exception e)
        {
            logger.error("Could not delete muted role!", e);
        }finally
        {
            Util.closeQuietly(connection);
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
        Connection connection = Database.getConnectionFromPool();
        if(connection == null)
        {
            logger.error("Could not set muted role!", new Exception());
            return;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, roleID);
            ps.setLong(3, roleID);
            ps.execute();
        }catch(Exception e)
        {
            logger.error("Could not set muted role!", e);
        }finally
        {
            Util.closeQuietly(connection);
        }
    }

    public Long getMutedRole(long guildID)
    {
        if(mutedRoles.containsKey(guildID))
        {
            return mutedRoles.get(guildID);
        }
        String qry = "SELECT roleID FROM mutedroles WHERE guildID = ?";
        Connection connection = Database.getConnectionFromPool();
        if(connection == null)
        {
            logger.error("Could not get muted-role!", new Exception());
            return null;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ResultSet rs = ps.executeQuery();
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
            logger.error("Could not initialize Muted Role Table!", e);
            return null;
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    private void init()
    {
        String qry = "CREATE TABLE IF NOT EXISTS mutedroles (guildID BIGINT PRIMARY KEY, roleID BIGINT)";
        Connection connection = Database.getConnectionFromPool();
        if(connection == null)
        {
            logger.error("Could not initialize MutedRole Table!", new Exception());
            return;
        }
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.execute();
        }catch(Exception e)
        {
            logger.error("Could not initialize Muted Role Table!", e);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }
}
