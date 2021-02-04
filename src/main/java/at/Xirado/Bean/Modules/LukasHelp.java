package at.Xirado.Bean.Modules;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.JDA;

public class LukasHelp extends Command
{
    public LukasHelp(JDA jda)
    {
        super(jda);
        this.invoke = "lukashelp";
        this.commandType = CommandType.FUN;
        this.description = "Lukas hilf uns";
        this.global = false;
        this.enabledGuilds = new Long[]{687748771760832551L};
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
