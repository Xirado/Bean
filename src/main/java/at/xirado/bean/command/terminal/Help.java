package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;

import java.util.Arrays;

public class Help extends ConsoleCommand
{
    public Help()
    {
        this.invoke = "help";
        this.aliases = Arrays.asList("?");
        this.description = "Shows a list of all commands";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        StringBuilder builder = new StringBuilder();
        int neededspaces = 0;

        for (ConsoleCommand ccmd : Bean.getInstance().getConsoleCommandManager().getCommands())
        {
            if (neededspaces < ccmd.getInvoke().length() + 2)
                neededspaces = ccmd.getInvoke().length() + 2;
        }

        builder.append("\n");

        for (ConsoleCommand ccmd : Bean.getInstance().getConsoleCommandManager().getCommands())
        {
            builder.append("|» ").append(ccmd.getInvoke());
            int remainingspaces = (neededspaces) - (ccmd.getInvoke().length());
            builder.append(" ".repeat(Math.max(0, remainingspaces)));
            builder.append("<->");
            builder.append("  ");
            builder.append(ccmd.description);
            builder.append("\n");
        }
        System.out.println(builder.toString().trim() + "\n");
    }
}
