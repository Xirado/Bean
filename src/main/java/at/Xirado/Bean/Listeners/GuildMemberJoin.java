package at.Xirado.Bean.Listeners;

import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


public class GuildMemberJoin extends ListenerAdapter
{

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e)
	{
		Member m = e.getMember();
		String qry = "SELECT * FROM modcases WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = 1";
		Connection con = SQL.getConnectionFromPool();
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
					Role role = e.getGuild().getRoleById(DiscordBot.getInstance().mutedRoleManager.getMutedRole(e.getGuild().getIdLong()));
					if(role != null) e.getGuild().addRoleToMember(m, role).queue(s -> {}, ex -> {});

				}
			}
		}catch (SQLException ignored)
		{

		}
	}
}
