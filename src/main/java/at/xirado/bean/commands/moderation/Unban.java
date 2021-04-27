package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.SQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

public class Unban extends Command
{

    public Unban(JDA jda)
    {
        super(jda);
        this.invoke = "unban";
        this.usage = "unban [@User/ID]";
        this.description = "Unbans an user";
        this.neededPermissions = Collections.singletonList(Permission.BAN_MEMBERS);
        this.neededBotPermissions = Collections.singletonList(Permission.BAN_MEMBERS);
        this.commandType = CommandType.MODERATION;
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        User u = event.getAuthor();
        Guild g = event.getGuild();
        Member m = context.getMember();
        Member bot = g.getMember(DiscordBot.instance.jda.getSelfUser());
        TextChannel c = event.getChannel();
        if (args.length != 1)
        {
            context.replyErrorUsage();
            return;
        }
        String ID = args[0].replaceAll("[^0-9]", "");
        if(ID.length() == 0)
        {
            context.replyError("ID may not be empty!");
            return;
        }
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (user) ->
                {
                    g.unban(user).queue(
                            (success) ->
                            {
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
                                    ps.setLong(2, user.getIdLong());
                                    ps.setString(3, "Tempban");
                                    ps.execute();
                                    connection.close();
                                }catch (SQLException ex)
                                {
                                    context.replyError(context.getLocalized("general.db_error"));
                                    return;
                                }
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(Color.green)
                                        .setDescription(context.getLocalized("commands.unban.has_been_unbanned", user.getAsMention()));
                                context.reply(builder.build());
                                if(context.hasLogChannel())
                                {
                                    EmbedBuilder builder2 = new EmbedBuilder()
                                            .setColor(Color.green)
                                            .setTitle("Unban")
                                            .setThumbnail(user.getEffectiveAvatarUrl())
                                            .addField(context.getLocalized("commands.target"), user.getAsMention()+" ("+user.getAsTag()+")" , true)
                                            .addField("Moderator", m.getAsMention()+" ("+m.getUser().getAsTag()+")", true);
                                    context.replyInLogChannel(builder2.build());
                                }
                            },
                            (error) ->
                            {
                                context.replyError(context.getLocalized("commands.unban.could_not_unban"));
                            }
                    );
                }
        );
    }
}
