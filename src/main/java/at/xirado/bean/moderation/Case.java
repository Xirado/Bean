package at.xirado.bean.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.function.Consumer;

public class Case
{

    private static final Logger logger = LoggerFactory.getLogger(Case.class);

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
        Connection connection = Database.getConnectionFromPool();
        if (connection == null) return;
        String qry = "UPDATE modcases SET active = ? WHERE caseID = ?";
        try (PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setBoolean(1, value);
            ps.setString(2, this.caseID);
            ps.execute();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        } finally
        {
            Util.closeQuietly(connection);
        }
    }


    public void setReason(String reason)
    {
        Connection connection = Database.getConnectionFromPool();
        if (connection == null) return;
        String qry = "UPDATE modcases SET reason = ? WHERE caseID = ?";
        try (PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setString(1, reason);
            ps.setString(2, this.caseID);
            ps.execute();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        } finally
        {
            Util.closeQuietly(connection);
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
        Connection connection = Database.getConnectionFromPool();
        if (connection == null) return false;
        String qry = "DELETE FROM modcases WHERE caseID = ?";
        try (PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setString(1, caseID);
            ps.execute();
            return true;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return false;
        } finally
        {
            Util.closeQuietly(connection);
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
        Connection connection = Database.getConnectionFromPool();
        if (connection == null) return null;
        String qry = "INSERT INTO modcases (caseID, guildID, targetID, moderatorID, caseType, reason, duration, creationDate, active) values (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setString(1, caseID);
            ps.setLong(2, guildID);
            ps.setLong(3, targetID);
            ps.setLong(4, moderatorID);
            ps.setString(5, caseType);
            ps.setString(6, reason);
            ps.setLong(7, duration);
            ps.setLong(8, System.currentTimeMillis());
            ps.setBoolean(9, true);
            ps.execute();
        } catch (SQLException throwables)
        {
            logger.error("Could not create case!", throwables);
            return null;
        } finally
        {
            Util.closeQuietly(connection);
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

    public static boolean idAlreadyExists(String ID)
    {
        Connection connection = Database.getConnectionFromPool();
        String qry = "SELECT 1 FROM modcases WHERE caseID = ?";
        if (connection == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setString(1, ID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return true;
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void banMember(long guildID, long targetID, long moderatorID, String reason, long duration, Consumer<Case> caseConsumer, Consumer<Throwable> throwableConsumer)
    {
        Guild guild = Bean.getInstance().getJDA().getGuildById(guildID);
        if (guild == null)
        {
            throwableConsumer.accept(new IllegalArgumentException("This guild does not exist!"));
            return;
        }
        Bean.getInstance().getJDA().openPrivateChannelById(targetID).queue(
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
