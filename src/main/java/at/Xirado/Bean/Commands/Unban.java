package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Unban extends Command
{

    public Unban(JDA jda)
    {
        super(jda);
        this.invoke = "unban";
        this.usage = "unban [@User/ID]";
        this.description = "Unbans a user";
        this.neededPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.commandType = CommandType.MODERATION;
    }

    @Override
    public void execute(CommandEvent e)
    {
        String[] args = e.getArguments().getArguments();
        Member m = e.getMember();
        User u = e.getAuthor();
        Guild g = e.getGuild();
        Member bot = g.getMember(DiscordBot.instance.jda.getSelfUser());
        TextChannel c = e.getChannel();
        if (!bot.hasPermission(Permission.BAN_MEMBERS))
        {
            c.sendMessage(u.getAsMention() + ", I don't have permission to do this.").queue(response -> response.delete().queueAfter(20, TimeUnit.SECONDS));
            return;
        }
        if (args.length != 1)
        {
            e.replyErrorUsage();
            return;
        }
        String ID = args[0].replaceAll("[^0-9]", "");
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (user) ->
                {
                    g.unban(user).queue(
                            (success) ->
                            {
                                EmbedBuilder b = new EmbedBuilder()
                                        .setColor(Color.decode("#FEFEFE"))
                                        .setDescription("☑ " + user.getAsTag() + " has been unbanned")
                                        .setTimestamp(Instant.now())
                                        .setFooter("ID: " + user.getIdLong());
                                c.sendMessage(b.build()).queue();
                            }
                    );
                }
        );
    }
}
