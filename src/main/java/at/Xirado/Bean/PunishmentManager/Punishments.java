package at.Xirado.Bean.PunishmentManager;

import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Punishments
{
    public static boolean hasActiveBan(Member m)
    {
        Connection connection = SQL.getConnectionFromPool();
        String qry = "SELECT 1 from modcases WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = ?";
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, m.getGuild().getIdLong());
            ps.setLong(2, m.getIdLong());
            ps.setString(3, "Ban");
            ps.setBoolean(4, true);
            ResultSet rs = ps.executeQuery();
            connection.close();
            return rs.next();
        }catch (SQLException ex)
        {
            Console.logger.error("Could not check if user has active ban", ex);
            return false;
        }
    }

    public static void unban(Case c, TextChannel alternate)
    {
        if(c == null) return;
        c.fetchUpdate();
        Guild g = DiscordBot.getInstance().jda.getGuildById(c.getGuildID());
        if(g == null) return;
        if(c.isActive())
        {
            g.unban(String.valueOf(c.getTargetID())).queue(
                    (unbanned) ->
                    {
                        c.setActive(false);
                        DiscordBot.getInstance().jda.retrieveUserById(c.getTargetID()).queue(
                                (targetuser) ->
                                {
                                    TextChannel logchannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                                    User selfuser = DiscordBot.getInstance().jda.getSelfUser();
                                    EmbedBuilder builder = new EmbedBuilder()
                                            .setColor(Color.green)
                                            .setTitle("Unban | #"+c.getCaseID())
                                            .addField("Target", targetuser.getAsMention()+" ("+targetuser.getAsTag()+")", true)
                                            .addField("Moderator", selfuser.getAsMention()+" ("+selfuser.getAsTag()+")", true)
                                            .setFooter("Auto-Unban ("+c.getReason()+")");
                                    if(logchannel == null)
                                    {
                                        if(alternate != null) alternate.sendMessage(builder.build()).queue();
                                    }else
                                    {
                                        logchannel.sendMessage(builder.build()).queue();
                                    }
                                },
                                (e) ->
                                {
                                }
                        );

                    },
                    (error) ->
                    {

                    }
            );
        }
    }

    public static void unmute(Case c, TextChannel alternativechannel)
    {
        c.fetchUpdate(); // fetch update from DB
        if(!c.isActive()) return;
        Guild g = DiscordBot.getInstance().jda.getGuildById(c.getGuildID());
        if(g == null) return;
        g.retrieveMemberById(c.getTargetID()).queue(
                (member) ->
                {
                    Role role = g.getRoleById(DiscordBot.getInstance().mutedRoleManager.getMutedRole(c.getGuildID()));
                    if(role == null) return;
                    if(!c.isActive() && member.getRoles().contains(role))
                    {
                        g.removeRoleFromMember(member, role).queue(s -> {}, e -> {});
                        return;
                    }
                    c.setActive(false);
                    g.removeRoleFromMember(member, role).queue(s -> {}, e -> {});
                    TextChannel logchannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                    User selfuser = DiscordBot.getInstance().jda.getSelfUser();
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.green)
                            .setTitle("Unmute | #"+c.getCaseID())
                            .addField("Target", "<@"+c.getTargetID()+"> ("+member.getUser().getAsTag()+")" , true)
                            .addField("Moderator", selfuser.getAsMention()+" ("+selfuser.getAsTag()+")", true)
                            .setFooter("Auto-Unmute ("+c.getReason()+")");
                    if(logchannel != null)
                    {
                        logchannel.sendMessage(builder.build()).queue();
                    }else
                    {
                        if(alternativechannel != null)
                        {
                            alternativechannel.sendMessage(builder.build()).queue();
                        }
                    }
                },
                (error) ->
                {

                }
        );
    }

    public static Case getActiveMuteCase(Member m)
    {
        Connection connection = SQL.getConnectionFromPool();
        String qry = "SELECT * from modcases WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = ?";
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, m.getGuild().getIdLong());
            ps.setLong(2, m.getIdLong());
            ps.setString(3, "Mute");
            ps.setBoolean(4, true);
            ResultSet rs = ps.executeQuery();
            connection.close();
            if(rs.next())
            {
                return new Case(CaseType.valueOf(rs.getString("caseType").toUpperCase()), rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active"));
            }
            return null;
        }catch (SQLException ex)
        {
            Console.logger.error("Could not check if member has active mute", ex);
            return null;
        }
    }

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
    public static Case getCaseByID(String sixDigitID, long guildID)
    {
        String qry = "SELECT * FROM modcases WHERE caseID = ? AND guildID = ? LIMIT 1";
        try
        {
            Connection connection = SQL.getConnectionFromPool();
            PreparedStatement ps = connection.prepareStatement(qry);
            ps.setString(1,sixDigitID.toUpperCase());
            ps.setLong(2, guildID);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                return new Case(CaseType.valueOf(rs.getString("caseType").toUpperCase()), rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active"));

            }
            return null;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return null;
        }
    }

    public static List<Case> getModlog(long guildID, long targetID, int limit)
    {
        String qry = "SELECT * FROM modcases WHERE guildID = ? AND targetID = ? ORDER BY creationDate DESC LIMIT ?";
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null) return new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, targetID);
            ps.setInt(3, limit);
            List<Case> cases = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                cases.add(new Case(CaseType.valueOf(rs.getString("caseType").toUpperCase()), rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active")));
            }
            Collections.reverse(cases);
            connection.close();
            return cases;
        }catch(SQLException e)
        {
            Console.logger.error("Could not get modlog!", e);
            return new ArrayList<>();
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
            Connection connection = SQL.getConnectionFromPool();
            PreparedStatement ps = connection.prepareStatement(qry);
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
            connection.close();
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
            Connection connection = SQL.getConnectionFromPool();
            PreparedStatement ps = connection.prepareStatement(qry);
            ps.setLong(1,guildID);
            ps.setLong(2,targetID);
            ps.setString(3, "Warn");
            List<Case> allCases = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                allCases.add(new Case(CaseType.WARN, rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active")));
            }
            connection.close();
            return allCases;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return new ArrayList<>();
        }
    }
}
