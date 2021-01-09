package at.Xirado.Bean.Modules;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.JDA;

public class GabrielHelp extends Command
{
    public GabrielHelp(JDA jda)
    {
        super(jda);
        this.invoke = "gabrielhelp";
        this.commandType = CommandType.FUN;
        this.description = "Gabriel hilf uns";
        this.global = false;
        this.enabledGuilds = new Long[]{687748771760832551L};
        this.usage = "gabrielhelp";
    }

    @Override
    public void execute(CommandEvent e) {
        String tosend = "<@355394733884833793>, wir brauchen dich!";
        for(int i = 0; i < 5; i++)
        {
            e.getChannel().sendMessage(tosend).queue(null, Util.handle(e.getChannel()));
        }

    }
}
