package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Mock extends Command
{

    public Mock(JDA jda)
    {
        super(jda);
        this.invoke = "mock";
        this.description = "gEt mOcKeD kId";
        this.commandType = CommandType.FUN;
        this.usage = "mock [Text]";
    }

    @Override
    public void executeCommand(CommandEvent e) {
        String[] args = e.getArguments().toStringArray();
        Member member = e.getMember();
        TextChannel channel = e.getChannel();
        Guild g = e.getGuild();
        if(g.getId().equalsIgnoreCase("713469621532885002"))
        {
            if(!member.hasPermission(Permission.MESSAGE_MANAGE))
            {
                if(!member.getRoles().contains(g.getRoleById(749958608497803327L)))
                {
                    channel.sendMessage(Util.NoPermissions(member)).queue((result) -> result.delete().queueAfter(5, TimeUnit.SECONDS), Util.handle(channel));
                    return;
                }
            }

        }
        e.getMessage().delete().queue();
        if(args.length < 1)
        {
            e.replyErrorUsage();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args)
        {
            sb.append(arg).append(" ");
        }
        String tomock = sb.toString();
        tomock = tomock.substring(0, tomock.length()-1);
        StringBuilder sensitive = new StringBuilder();
        for(int i = 0; i < tomock.length(); i++)
        {
            if(i%2 == 0)
            {
                sensitive.append(String.valueOf(tomock.charAt(i)).toLowerCase());
            }
            else
            {
                sensitive.append(String.valueOf(tomock.charAt(i)).toUpperCase());
            }
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setFooter("requested by "+e.getAuthor().getAsTag())
                .setDescription("<:mock:773566020588666961> "+sensitive.toString()+" <:mock:773566020588666961>");
        channel.sendMessage(builder.build()).queue();

    }
}
