package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Mock extends Command
{

    public Mock()
    {
        super("mock", "vAcCiNeS cAuSe AuTiSm", "mock [Text]");
        setCommandCategory(CommandCategory.FUN);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
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
        channel.sendMessage("<:mock:773566020588666961> "+ sensitive +" <:mock:773566020588666961>").queue();

    }
}
