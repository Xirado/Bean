package at.Xirado.Bean.PunishmentManager;

import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.SQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Random;

public class Case
{

    /**
     * caseID VARCHAR(6)
     * guildID BIGINT NOT NULL
     * targetID BIGINT NOT NULL
     * moderatorID BIGINT NOT NULL
     * caseType VARCHAR(20) NOT NULL
     * reason VARCHAR(512) NOT NULL
     * duration BIGINT NOT NULL
     * creationDate BIGINT NOT NULL
     */
    private String caseID;
    private CaseType type;
    private long GuildID;
    private long targetID;
    private long moderatorID;
    private String reason;
    private long duration;
    private long createdAt;

    public CaseType getType()
    {
        return type;
    }

    public String getCaseID()
    {
        return caseID;
    }
    public long getGuildID()
    {
        return GuildID;
    }

    public long getTargetID()
    {
        return targetID;
    }

    public long getModeratorID()
    {
        return moderatorID;
    }

    public String getReason()
    {
        return reason;
    }

    public long getDuration()
    {
        return duration;
    }

    public long getCreatedAt()
    {
        return createdAt;
    }

    public Case(CaseType type, long GuildID, long targetID, long moderatorID, String reason, long duration, long createdAt, String caseID)
    {
        this.type = type;
        this.GuildID = GuildID;
        this.targetID = targetID;
        this.moderatorID = moderatorID;
        this.reason = reason;
        this.duration = duration;
        this.caseID = caseID;
        this.createdAt = createdAt;
    }
    public static Case createCase(CaseType type, long guildID, long targetID, long moderatorID, String reason, long duration)
    {
        String caseType = type.getFriendlyName();
        String caseID = null;
        while(caseID == null)
        {
            String generatedID = generateCaseNumber();
            if(!idAlreadyExists(generatedID)) caseID = generatedID;
        }
        try
        {
            String qry = "INSERT INTO modcases (caseID, guildID, targetID, moderatorID, caseType, reason, duration, creationDate) values (?,?,?,?,?,?,?,?)";
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setString(1, caseID);
            ps.setLong(2, guildID);
            ps.setLong(3, targetID);
            ps.setLong(4, moderatorID);
            ps.setString(5, caseType);
            ps.setString(6, reason);
            ps.setLong(7, duration);
            ps.setLong(8, System.currentTimeMillis());
            ps.execute();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return new Case(type,guildID,targetID,moderatorID,reason,duration,System.currentTimeMillis(), caseID);
    }



    public static String generateCaseNumber()
    {
        Random random = new Random();
        String first = Integer.toString(random.nextInt(256), 36);
        if(first.length() == 1) first = "0"+first;
        String second = Integer.toString(random.nextInt(256), 36);
        if(second.length() == 1) second = "0"+second;
        String third = Integer.toString(random.nextInt(256), 36);
        if(third.length() == 1) third = "0"+third;
        String code = first+second+third;
        return code.toUpperCase();
    }

    public static boolean idAlreadyExists(String ID)
    {
        try
        {
            String qry = "SELECT 1 FROM modcases WHERE caseID = ?";
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setString(1,ID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch (SQLException e)
        {
            e.printStackTrace();
            return true;
        }
    }
}
