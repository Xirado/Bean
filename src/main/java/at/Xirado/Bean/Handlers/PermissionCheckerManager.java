package at.Xirado.Bean.Handlers;

import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionCheckerManager
{

    private final ConcurrentHashMap<Long, ArrayList<Long>> moderators;

    public PermissionCheckerManager()
    {

        moderators = new ConcurrentHashMap<>();
        initializeDBTable();

    }

    public void reloadAllowedRoles(long guildID)
    {
        if(!moderators.containsKey(guildID)) return;
        moderators.remove(guildID);
        getAllowedRoles(guildID);

    }

    private void initializeDBTable()
    {
        Connection con = SQL.getConnectionFromPool();
        if(con == null)
        {
            Console.logger.error("Could not initialize Database Table!", new Exception());
            return;
        }
        String qry = "CREATE TABLE IF NOT EXISTS moderatorroles (guildID BIGINT, roleID BIGINT)";
        try(PreparedStatement ps = con.prepareStatement(qry))
        {
            ps.execute();
            con.close();
        }catch (SQLException e)
        {
            Console.logger.error("Could not initialize Database Table!", e);
        }
    }

    public boolean isAllowedRole(long guildID, long roleID)
    {
        if(moderators.containsKey(guildID))
        {
            ArrayList<Long> allowedRoles = moderators.get(guildID);
            return allowedRoles.contains(roleID);
        }
        Connection con = SQL.getConnectionFromPool();
        if(con == null)
        {
            Console.logger.error("Could not check if \""+roleID+"\" is a valid moderator role in \""+guildID+"\"", new Exception());
            return false;
        }
        String qry = "SELECT 1 FROM moderatorroles WHERE guildID = ? AND roleID = ?";
        try(PreparedStatement ps = con.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, roleID);
            ResultSet rs = ps.executeQuery();
            con.close();
            return rs.next();
        }catch (SQLException e)
        {
            Console.logger.error("Could not check if \""+roleID+"\" is a valid moderator role in \""+guildID+"\"", e);
            return false;
        }
    }

    public boolean addAllowedRole(long guildID, long roleID)
    {
        if(isAllowedRole(guildID, roleID)) return true;
        Connection con = SQL.getConnectionFromPool();
        if(con == null)
        {
            Console.logger.error("Could not add moderator role \""+roleID+"\" to guild \""+guildID+"\"!", new Exception());
            return false;
        }
        String qry = "INSERT INTO moderatorroles (guildID, roleID) values (?,?)";
        try(PreparedStatement ps = con.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, roleID);
            ps.execute();
            con.close();
            reloadAllowedRoles(guildID);
            return true;
        }catch (SQLException e)
        {
            Console.logger.error("Could not add moderator role \""+roleID+"\" to guild \""+guildID+"\"!", e);
            return false;
        }
    }

    public boolean isModerator(Member m)
    {
        if(m.hasPermission(Permission.ADMINISTRATOR)) return true;
        long guildID = m.getGuild().getIdLong();
        for(Role r : m.getRoles())
        {
            if(isAllowedRole(guildID, r.getIdLong()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean removeAllowedRole(long guildID, long roleID)
    {
        if(!isAllowedRole(guildID, roleID)) return true;
        Connection con = SQL.getConnectionFromPool();
        if(con == null)
        {
            Console.logger.error("Could not aremove moderator role \""+roleID+"\" from guild \""+guildID+"\"!", new Exception());
            return false;
        }
        String qry = "DELETE FROM moderatorroles WHERE guildID = ? AND roleID = ?";
        try(PreparedStatement ps = con.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, roleID);
            ps.execute();
            con.close();
            reloadAllowedRoles(guildID);
            return true;
        }catch (SQLException e)
        {
            Console.logger.error("Could not remove moderator role \""+roleID+"\" from guild \""+guildID+"\"!", e);
            return false;
        }
    }

    public ArrayList<Long> getAllowedRoles(long guildID)
    {
        if(moderators.containsKey(guildID))
        {
           return moderators.get(guildID);
        }
        Connection con = SQL.getConnectionFromPool();
        if(con == null)
        {
            Console.logger.error("Could not fetch allowed roles for guild \""+guildID+"\"", new Exception());
            return new ArrayList<>();
        }
        String qry = "SELECT roleID FROM moderatorroles WHERE guildID = ?";
        try(PreparedStatement ps = con.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ResultSet rs = ps.executeQuery();
            ArrayList<Long> ret = new ArrayList<>();
            while(rs.next())
            {
                ret.add(rs.getLong("roleID"));
            }
            con.close();
            moderators.put(guildID, ret);
            return ret;
        }catch (SQLException e)
        {
            Console.logger.error("Could not fetch allowed roles for guild \""+guildID+"\"", e);
            return new ArrayList<>();
        }
    }
}
