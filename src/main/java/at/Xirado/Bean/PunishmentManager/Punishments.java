package at.Xirado.Bean.PunishmentManager;

import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Punishments
{

    public static List<Case> getAllInfractions(Member member)
    {
        long guildID = member.getGuild().getIdLong();
        long targetID = member.getIdLong();
        String qry = "SELECT * FROM modcases WHERE guildID = ? AND targetID = ? AND caseType = ?";
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
                allCases.add(new Case(CaseType.WARN, rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID")));
            }
            return allCases;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return new ArrayList<>();
        }
    }
}
