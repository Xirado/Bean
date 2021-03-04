package at.Xirado.Bean.Commands.Moderation;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

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
    public void executeCommand(CommandEvent e)
    {
        String[] args = e.getArguments().toStringArray();
        User u = e.getAuthor();
        Guild g = e.getGuild();
        Member m = e.getMember();
        Member bot = g.getMember(DiscordBot.instance.jda.getSelfUser());
        TextChannel c = e.getChannel();
        if (args.length != 1)
        {
            e.replyErrorUsage();
            return;
        }
        String ID = args[0].replaceAll("[^0-9]", "");
        if(ID.length() == 0)
        {
            e.replyError("ID may not be empty!");
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
                                    e.replyError("We currently have an issue with our database. Please try again later!");
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
                                    e.replyError("We currently have an issue with our database. Please try again later!");
                                }
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(Color.green)
                                        .setDescription(user.getAsMention()+" has been unbanned!");
                                e.reply(builder.build());
                                if(e.hasLogChannel())
                                {
                                    EmbedBuilder builder2 = new EmbedBuilder()
                                            .setColor(Color.green)
                                            .setTitle("Unban")
                                            .setThumbnail(user.getEffectiveAvatarUrl())
                                            .addField("Target", user.getAsMention()+" ("+user.getAsTag()+")" , true)
                                            .addField("Moderator", m.getAsMention()+" ("+m.getUser().getAsTag()+")", true);
                                    e.replyinLogChannel(builder2.build());
                                }
                            },
                            (error) ->
                            {
                                e.replyError("Could not unban user!");
                            }
                    );
                }
        );
    }
}
