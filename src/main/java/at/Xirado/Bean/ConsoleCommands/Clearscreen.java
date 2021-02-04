package at.Xirado.Bean.ConsoleCommands;

import at.Xirado.Bean.CommandManager.ConsoleCommand;
import at.Xirado.Bean.Logging.Shell;
import org.jline.utils.InfoCmp;

public class Clearscreen extends ConsoleCommand
{
    public Clearscreen()
    {
        this.invoke = "cls";
    }
    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if(args.length == 0)
        {
            Shell.terminal.puts(InfoCmp.Capability.clear_screen);
            Shell.terminal.flush();
            return;
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("-f"))
        {
            Shell.terminal.flush();
        }

    }
}
