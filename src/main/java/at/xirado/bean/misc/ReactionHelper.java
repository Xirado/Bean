package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReactionHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ReactionHelper.class);

	public static Role getRoleIfAvailable(Long messageID, String emoticon)
	{
		Connection connection = SQL.getConnectionFromPool();
		if(connection == null)
		{
			return null;
		}
		try(PreparedStatement ps = connection.prepareStatement("SELECT roleID FROM reactionRoles WHERE messageID = ? AND emoticon = ?"))
		{
			ps.setLong(1, messageID);
			ps.setString(2, emoticon);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				connection.close();
				return Bean.instance.jda.getRoleById(rs.getLong("roleID"));
			}
			return null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		} finally
		{
			Util.closeQuietly(connection);
		}
	}
	public static void removeAllReactions(Long messageid)
	{
		Connection connection = SQL.getConnectionFromPool();
		if(connection == null) return;
		try(PreparedStatement ps = connection.prepareStatement("DELETE FROM reactionRoles WHERE messageID = ?"))
		{

			ps.setLong(1, messageid);
			ps.execute();
		} catch (SQLException e)
		{
			LOGGER.error("An error occured", e);
		}finally
		{
			Util.closeQuietly(connection);
		}
	}
	public static boolean duplicateExists(long messageid, String emoticon)
	{
		Connection connection = SQL.getConnectionFromPool();
		if(connection == null)
		{
			return true;
		}
		try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM reactionRoles WHERE messageid = ? AND emoticon = ?"))
		{
			ps.setLong(1, messageid);
			ps.setString(2, emoticon);
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e)
		{
			e.printStackTrace();
			return true;
		}finally
		{
			Util.closeQuietly(connection);
		}
	}
	public static void addReaction(long messageid, String emoticon, long roleid)
	{
		if(duplicateExists(messageid, emoticon))
			return;
		Connection connection = SQL.getConnectionFromPool();
		if(connection == null)
		{
			return;
		}
		try(PreparedStatement ps = connection.prepareStatement("INSERT INTO reactionRoles (messageID, emoticon, roleID) values (?,?,?)"))
		{
			ps.setLong(1, messageid);
			ps.setString(2, emoticon);
			ps.setLong(3, roleid);
			ps.execute();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}finally
		{
			Util.closeQuietly(connection);
		}
	}
	public static void removeReaction(long messageid, String emoticon, long roleid)
	{

		Connection connection = SQL.getConnectionFromPool();
		if(connection == null) return;
		try(PreparedStatement ps = connection.prepareStatement("DELETE FROM reactionRoles WHERE messageID = ? AND emoticon = ? AND roleID = ?"))
		{
			ps.setLong(1, messageid);
			ps.setString(2, emoticon);
			ps.setLong(3, roleid);
			ps.execute();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}finally
		{
			Util.closeQuietly(connection);
		}
	}

	public static ArrayList<HashMap<String,Long>> getAllReactions(Long messageID)
	{
		Connection connection = SQL.getConnectionFromPool();
		if(connection == null) return null;
		try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM reactionRoles WHERE messageID = ?"))
		{

			ps.setLong(1, messageID);
			ResultSet rs = ps.executeQuery();
			ArrayList<HashMap<String,Long>> allReactions = new ArrayList<>();
			while(rs.next())
			{
				String emoticon = rs.getString("emoticon");
				long roleID = rs.getLong("roleID");
				HashMap<String, Long> current = new HashMap<>();
				current.put(emoticon,roleID);
				allReactions.add(current);
			}
			return allReactions;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}finally
		{
			Util.closeQuietly(connection);
		}
	}
}
