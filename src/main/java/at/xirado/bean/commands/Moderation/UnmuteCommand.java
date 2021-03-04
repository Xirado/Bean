package at.xirado.bean.commands.Moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.SQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class UnmuteCommand extends Command
{
    public UnmuteCommand(JDA jda)
    {
        super(jda);
        this.invoke = "unmute";
        this.commandType = CommandType.MODERATION;
        this.description = "Unmutes a member";
        this.usage = "unmute [@Member/ID]";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        Guild g = event.getGuild();
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            event.replyError("You are not permissed to do this!");
            return;
        }
        String[] args = event.getArguments().toStringArray();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
        {
            event.replyError("User-ID may not be empty!");
            return;
        }
        g.retrieveMemberById(target_ID).queue(
                (targetMember) ->
                {
                    Role r = g.getRoleById(DiscordBot.getInstance().mutedRoleManager.getMutedRole(g.getIdLong()));
                    if(!targetMember.getRoles().contains(r))
                    {
                        event.replyError("This member is not muted!");
                        return;
                    }
                    g.removeRoleFromMember(targetMember, r).queue(s -> {}, e -> {});
                    String qry = "UPDATE modcases SET active = 0 WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = 1";
                    Connection connection = SQL.getConnectionFromPool();
                    if(connection == null)
                    {
                        event.replyError("We currently have an issue with our database. Please try again later!");
                        return;
                    }
                    try(var ps = connection.prepareStatement(qry))
                    {
                        ps.setLong(1, g.getIdLong());
                        ps.setLong(2, targetMember.getIdLong());
                        ps.setString(3, "Mute");
                        ps.execute();
                        connection.close();
                    }catch (SQLException ex)
                    {
                        event.replyError("We currently have an issue with our database. Please try again later!");
                    }
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.green)
                            .setDescription(targetMember.getAsMention()+" has been unmuted!");
                    event.reply(builder.build());
                    if(event.hasLogChannel())
                    {
                        EmbedBuilder builder2 = new EmbedBuilder()
                                .setColor(Color.green)
                                .setTitle("Unmute")
                                .setThumbnail(targetMember.getUser().getEffectiveAvatarUrl())
                                .addField("Target", targetMember.getAsMention()+" ("+targetMember.getUser().getAsTag()+")" , true)
                                .addField("Moderator", m.getAsMention()+" ("+m.getUser().getAsTag()+")", true);
                        event.replyinLogChannel(builder2.build());
                    }
                }
        );

    }
}
