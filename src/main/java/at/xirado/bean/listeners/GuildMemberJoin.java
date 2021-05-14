package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


public class GuildMemberJoin extends ListenerAdapter
{

	private static final Logger LOGGER = LoggerFactory.getLogger(GuildMemberJoin.class);

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e)
	{
		Member m = e.getMember();
		String qry = "SELECT * FROM modcases WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = 1";
		Connection con = Database.getConnectionFromPool();
		if(con == null)
		{
			LOGGER.error("Could not retrieve Connection!", new Exception());
			return;
		}
		try(var ps = con.prepareStatement(qry))
		{
			ps.setLong(1, e.getGuild().getIdLong());
			ps.setLong(2, m.getIdLong());
			ps.setString(3, "Mute");
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				long duration = rs.getLong("duration");
				long creationDate = rs.getLong("creationDate");
				if(creationDate+duration > System.currentTimeMillis())
				{
					Role role = e.getGuild().getRoleById(Bean.getInstance().mutedRoleManager.getMutedRole(e.getGuild().getIdLong()));
					if(role != null) e.getGuild().addRoleToMember(m, role).queue(s -> {}, ex -> {});

				}
			}
		}catch (SQLException exception)
		{
			LOGGER.error("Could not execute GuildMemberJoinEvent!", exception);
		}finally{
			Util.closeQuietly(con);
		}
	}
}
