package at.xirado.bean.command.terminal;

import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.log.Shell;
import org.jline.utils.InfoCmp;

import java.util.Arrays;

public class Clearscreen extends ConsoleCommand
{
    public Clearscreen()
    {
        this.invoke = "cls";
        this.description = "Clears the screen";
        this.aliases = Arrays.asList("clear", "clearscreen");
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if (args.length == 0)
        {
            Shell.terminal.puts(InfoCmp.Capability.clear_screen);
            Shell.terminal.flush();
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("-f"))
        {
            Shell.terminal.flush();
        }

    }
}
