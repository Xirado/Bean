package at.Xirado.Bean.PunishmentManager;

import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Punishments
{
    public static void createMutedRole(Guild g)
    {
        g.createRole()
                .setName("Muted")
                .setPermissions(0L)
                .queue(
                        (mutedRole) ->
                        {
                            for(TextChannel channel : g.getTextChannels())
                            {
                                channel.createPermissionOverride(mutedRole)
                                        .setDeny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION)
                                        .queue();
                            }
                        },
                        (error) ->
                        {
                            error.printStackTrace();
                        }
                );
    }
    public static Case getCaseByID(String sixDigitID, Guild guild)
    {
        String qry = "SELECT * FROM modcases WHERE caseID = ? AND guildID = ? LIMIT 1";
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setString(1,sixDigitID.toUpperCase());
            ps.setLong(2, guild.getIdLong());
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                return new Case(CaseType.WARN, rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active"));

            }
            return null;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return null;
        }
    }

    public static List<Case> getAllInfractions(Member member, long after, int limit)
    {
        long guildID = member.getGuild().getIdLong();
        long targetID = member.getIdLong();
        String usingLimit = "";
        if(limit >=0)
        {
            usingLimit = " LIMIT "+limit;
        }
        String qry = "SELECT * FROM modcases WHERE guildID = ? AND targetID = ? AND caseType = ? AND creationDate > ? ORDER BY creationDate ASC"+usingLimit;
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setLong(1,guildID);
            ps.setLong(2,targetID);
            ps.setString(3, "Warn");
            ps.setLong(4,after);
            List<Case> allCases = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                allCases.add(new Case(CaseType.WARN, rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active")));
            }
            return allCases;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static List<Case> getAllInfractions(Member member, int limit)
    {
        long guildID = member.getGuild().getIdLong();
        long targetID = member.getIdLong();
        String usingLimit = "";
        if(limit >=0)
        {
            usingLimit = " LIMIT "+limit;
        }
        String qry = "SELECT * FROM modcases WHERE guildID = ? AND targetID = ? AND caseType = ? ORDER BY creationDate ASC"+usingLimit;
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setLong(1,guildID);
            ps.setLong(2,targetID);
            ps.setString(3, "Warn");
            List<Case> allCases = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                allCases.add(new Case(CaseType.WARN, rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active")));
            }
            return allCases;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return new ArrayList<>();
        }
    }
}
