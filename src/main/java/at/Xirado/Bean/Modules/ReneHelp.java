package at.Xirado.Bean.Modules;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.JDA;

public class ReneHelp extends Command
{
    public ReneHelp(JDA jda)
    {
        super(jda);
        this.invoke = "renehelp";
        this.commandType = CommandType.FUN;
        this.description = "Rene hilf uns";
        this.global = false;
        this.enabledGuilds = new Long[]{687748771760832551L};
        this.usage = "renehelp";
    }

    @Override
    public void execute(CommandEvent e) {
        String tosend = "<@355769135260762114>, wir brauchen dich!";
        for(int i = 0; i < 5; i++)
        {
            e.getChannel().sendMessage(tosend).queue();
        }

    }
}
