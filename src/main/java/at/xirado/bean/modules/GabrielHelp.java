package at.xirado.bean.modules;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.JDA;

import java.util.Arrays;

public class GabrielHelp extends Command
{
    public GabrielHelp(JDA jda)
    {
        super(jda);
        this.invoke = "gabrielhelp";
        this.commandType = CommandType.FUN;
        this.description = "Gabriel hilf uns";
        this.global = false;
        this.enabledGuilds = Arrays.asList(687748771760832551L);
        this.usage = "gabrielhelp";
    }

    @Override
    public void executeCommand(CommandEvent e) {
        String tosend = "<@355394733884833793>, wir brauchen dich!";
        for(int i = 0; i < 5; i++)
        {
            e.getChannel().sendMessage(tosend).queue(null, Util.handle(e.getChannel()));
        }

    }
}
