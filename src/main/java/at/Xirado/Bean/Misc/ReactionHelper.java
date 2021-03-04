package at.Xirado.Bean.Misc;

import at.Xirado.Bean.Main.DiscordBot;
import net.dv8tion.jda.api.entities.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReactionHelper
{
	public static Role getRoleIfAvailable(Long messageID, String emoticon)
	{
		try
		{
			Connection connection = SQL.getConnectionFromPool();
			PreparedStatement ps = connection.prepareStatement("SELECT roleID FROM reactionRoles WHERE messageID = ? AND emoticon = ?");
			ps.setLong(1, messageID);
			ps.setString(2, emoticon);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				connection.close();
				return DiscordBot.instance.jda.getRoleById(rs.getLong("roleID"));
			}
			connection.close();
			return null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static void removeAllReactions(Long messageid)
	{
		try
		{
			Connection connection = SQL.getConnectionFromPool();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM reactionRoles WHERE messageID = ?");
			ps.setLong(1, messageid);
			ps.execute();
			connection.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	public static boolean duplicateExists(long messageid, String emoticon)
	{
		try
		{
			Connection connection = SQL.getConnectionFromPool();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM reactionRoles WHERE messageid = ? AND emoticon = ?");
			ps.setLong(1, messageid);
			ps.setString(2, emoticon);
			ResultSet rs = ps.executeQuery();
			connection.close();
			return rs.next();
		} catch (SQLException e)
		{
			e.printStackTrace();
			return true;
		}
	}
	public static void addReaction(long messageid, String emoticon, long roleid)
	{
		if(duplicateExists(messageid, emoticon))
			return;
		try
		{
			Connection connection = SQL.getConnectionFromPool();
			PreparedStatement ps = connection.prepareStatement("INSERT INTO reactionRoles (messageID, emoticon, roleID) values (?,?,?)");
			ps.setLong(1, messageid);
			ps.setString(2, emoticon);
			ps.setLong(3, roleid);
			ps.execute();
			connection.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	public static void removeReaction(long messageid, String emoticon, long roleid)
	{
		try
		{
			Connection connection = SQL.getConnectionFromPool();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM reactionRoles WHERE messageID = ? AND emoticon = ? AND roleID = ?");
			ps.setLong(1, messageid);
			ps.setString(2, emoticon);
			ps.setLong(3, roleid);
			ps.execute();
			connection.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<HashMap<String,Long>> getAllReactions(Long messageID)
	{
		try
		{
			Connection connection = SQL.getConnectionFromPool();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM reactionRoles WHERE messageID = ?");
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
			connection.close();
			return allReactions;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
}
