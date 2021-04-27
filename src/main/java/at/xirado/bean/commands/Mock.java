package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        Member member = context.getMember();
        TextChannel channel = event.getChannel();
        event.getMessage().delete().queue();
        if(args.length < 1)
        {
            context.replyErrorUsage();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args)
        {
            sb.append(arg).append(" ");
        }
        String tomock = sb.toString().replaceAll("ß", "ss");
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
                .setFooter("requested by "+event.getAuthor().getAsTag())
                .setDescription("<:mock:773566020588666961> "+ sensitive +" <:mock:773566020588666961>");
        channel.sendMessage(builder.build()).queue();

    }
}
