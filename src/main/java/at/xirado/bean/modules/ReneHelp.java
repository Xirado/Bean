package at.xirado.bean.modules;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class ReneHelp extends Command
{
    public ReneHelp(JDA jda)
    {
        super(jda);
        this.invoke = "renehelp";
        this.commandType = CommandType.FUN;
        this.description = "Rene hilf uns";
        this.global = false;
        this.enabledGuilds = Arrays.asList(687748771760832551L);
        this.usage = "renehelp";
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String toSend = "<@355769135260762114>, wir brauchen dich!";
        for(int i = 0; i < 5; i++)
        {
            event.getChannel().sendMessage(toSend).queue();
        }

    }
}
