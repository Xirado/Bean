package at.xirado.bean.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.data.database.SQLBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.function.Consumer;

public class Case
{

    private static final Logger logger = LoggerFactory.getLogger(Case.class);

    private final String caseID;
    private final CaseType type;
    private final long GuildID;
    private final long targetID;
    private final long moderatorID;
    private String reason;
    private long duration;
    private final long createdAt;
    private boolean isActive;

    public void setActive(boolean value)
    {

        String qry = "UPDATE modcases SET active = ? WHERE caseID = ?";
        var query = new SQLBuilder(qry)
                .addParameters(value, this.caseID);
        try
        {
            query.execute();
        }catch (SQLException ex)
        {
            logger.error("Could not set active to "+value+"!", ex);
        }
    }


    public void setReason(String reason)
    {
        String qry = "UPDATE modcases SET reason = ? WHERE caseID = ?";
        var query = new SQLBuilder(qry)
                .addParameters(reason, this.caseID);
        try
        {
            query.execute();
        }catch (SQLException ex)
        {
            logger.error("Could not set reason of modcase!", ex);
        }
    }

    public boolean isActive()
    {
        return isActive;
    }

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

    public void fetchUpdate()
    {
        Case newcase = Punishments.getCaseByID(this.getCaseID(), this.getGuildID());
        if (newcase != null)
        {
            this.isActive = newcase.isActive;
            this.duration = newcase.duration;
            this.reason = newcase.reason;
        }

    }

    /**
     * Deletes a case
     *
     * @return returns true if the case has been deleted successfully
     */
    public boolean deleteCase()
    {
        String qry = "DELETE FROM modcases WHERE caseID = ?";
        var query = new SQLBuilder(qry)
                .addParameters(caseID);
        try
        {
            query.execute();
            return true;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return false;
        }
    }

    public Case(CaseType type, long GuildID, long targetID, long moderatorID, String reason, long duration, long createdAt, String caseID, boolean isActive)
    {
        this.type = type;
        this.GuildID = GuildID;
        this.targetID = targetID;
        this.moderatorID = moderatorID;
        this.reason = reason;
        this.duration = duration;
        this.caseID = caseID;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public static Case createCase(CaseType type, long guildID, long targetID, long moderatorID, String reason, long duration)
    {
        String caseType = type.getFriendlyName();
        String caseID = null;
        while (caseID == null)
        {
            String generatedID = generateCaseNumber();
            if (!idAlreadyExists(generatedID)) caseID = generatedID;
        }
        String qry = "INSERT INTO modcases (caseID, guildID, targetID, moderatorID, caseType, reason, duration, creationDate, active) values (?,?,?,?,?,?,?,?,?)";
        var query = new SQLBuilder(qry)
                .addParameters(caseID, guildID, targetID, moderatorID, caseType, reason, duration, System.currentTimeMillis(), true);
        try
        {
            query.execute();
        } catch (SQLException throwables)
        {
            logger.error("Could not create case!", throwables);
            return null;
        }
        return new Case(type, guildID, targetID, moderatorID, reason, duration, System.currentTimeMillis(), caseID, true);
    }


    public static String generateCaseNumber()
    {
        Random random = new Random();
        String first = Integer.toString(random.nextInt(1295), 36);
        if (first.length() == 1) first = "0" + first;
        String second = Integer.toString(random.nextInt(1295), 36);
        if (second.length() == 1) second = "0" + second;
        String third = Integer.toString(random.nextInt(1295), 36);
        if (third.length() == 1) third = "0" + third;
        String code = first + second + third;
        return code.toUpperCase();
    }

    public static boolean idAlreadyExists(String id)
    {
        String qry = "SELECT 1 FROM modcases WHERE caseID = ?";
        var query = new SQLBuilder(qry)
                .addParameter(id);
        try(ResultSet rs = query.executeQuery())
        {
            return rs.next();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return true;
        }
    }

    public static void banMember(long guildID, long targetID, long moderatorID, String reason, long duration, Consumer<Case> caseConsumer, Consumer<Throwable> throwableConsumer)
    {
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
        if (guild == null)
        {
            throwableConsumer.accept(new IllegalArgumentException("This guild does not exist!"));
            return;
        }
        Bean.getInstance().getShardManager().getShards().get(0).openPrivateChannelById(targetID).queue(
                privateChannel ->
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(CaseType.BAN.getEmbedColor())
                            .setTitle("You have been banned from " + guild.getName() + "!")
                            .addField("Reason", reason, true)
                            .addField("Moderator", "<@" + moderatorID + ">", true);
                    privateChannel.sendMessage(builder.build()).queue(
                            s ->
                            {
                                guild.ban(String.valueOf(guildID), 0, reason).queue(
                                        s1 ->
                                        {
                                            Case banCase = createCase(CaseType.BAN, guildID, targetID, moderatorID, reason, duration);
                                            caseConsumer.accept(banCase);
                                        }, throwableConsumer
                                );
                            },
                            e ->
                            {

                            }
                    );
                }, throwableConsumer
        );
        guild.ban(String.valueOf(targetID), 0, reason).queue();
    }

}
