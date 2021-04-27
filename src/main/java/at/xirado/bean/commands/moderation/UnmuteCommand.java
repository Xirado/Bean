package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Member m = context.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        Guild g = event.getGuild();
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            context.replyError(context.getLocalized("general.no_perms"));
            return;
        }
        String[] args = context.getArguments().toStringArray();
        if(args.length < 1)
        {
            context.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
        {
            context.replyError(context.getLocalized("commands.id_empty"));
            return;
        }
        g.retrieveMemberById(target_ID).queue(
                (targetMember) ->
                {
                    Role r = g.getRoleById(DiscordBot.getInstance().mutedRoleManager.getMutedRole(g.getIdLong()));
                    if(!targetMember.getRoles().contains(r))
                    {
                        context.replyError(context.getLocalized("commands.unmute.member_not_muted"));
                        return;
                    }
                    g.removeRoleFromMember(targetMember, r).queue(s -> {}, e -> {});
                    String qry = "UPDATE modcases SET active = 0 WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = 1";
                    Connection connection = SQL.getConnectionFromPool();
                    if(connection == null)
                    {
                        context.replyError(context.getLocalized("general.db_error"));
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
                        context.replyError(context.getLocalized("general.db_error"));
                        return;
                    }
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.green)
                            .setDescription(context.getLocalized("commands.unmute.user_unmuted", targetMember.getAsMention()));
                    context.reply(builder.build());
                    if(context.hasLogChannel())
                    {
                        EmbedBuilder builder2 = new EmbedBuilder()
                                .setColor(Color.green)
                                .setTitle("Unmute")
                                .setThumbnail(targetMember.getUser().getEffectiveAvatarUrl())
                                .addField(context.getLocalized("commands.target"), targetMember.getAsMention()+" ("+targetMember.getUser().getAsTag()+")" , true)
                                .addField("Moderator", m.getAsMention()+" ("+m.getUser().getAsTag()+")", true);
                        context.replyInLogChannel(builder2.build());
                    }
                }
        );

    }
}
