package at.xirado.bean.modules;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.JDA;

import java.util.Arrays;

public class LukasHelp extends Command
{
    public LukasHelp(JDA jda)
    {
        super(jda);
        this.invoke = "lukashelp";
        this.commandType = CommandType.FUN;
        this.description = "Lukas hilf uns";
        this.global = false;
        this.enabledGuilds = Arrays.asList(687748771760832551L);
        this.usage = "lukashelp";
    }

    @Override
    public void executeCommand(CommandEvent e) {
        String tosend = "<@614384531423756298>, wir brauchen dich!";
        for(int i = 0; i < 5; i++)
        {
            e.getChannel().sendMessage(tosend).queue(null, Util.handle(e.getChannel()));
        }

    }
}
