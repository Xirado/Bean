package at.xirado.bean.consolecommands;

import at.xirado.bean.commandmanager.ConsoleCommand;
import at.xirado.bean.main.DiscordBot;

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
        for(ConsoleCommand ccmd : DiscordBot.instance.consoleCommandManager.consoleCommands)
        {
            if(neededspaces < ccmd.getInvoke().length()+2)
            {
                neededspaces = ccmd.getInvoke().length()+2;
            }
        }
        builder.append("\n");
        for(ConsoleCommand ccmd : DiscordBot.instance.consoleCommandManager.consoleCommands)
        {
            builder.append("|» ").append(ccmd.getInvoke());
            int remainingspaces = (neededspaces)-(ccmd.getInvoke().length());
            builder.append(" ".repeat(Math.max(0, remainingspaces)));
            builder.append("<->");
            builder.append("  ");
            builder.append(ccmd.description);
            builder.append("\n");
        }
        System.out.println(builder.toString().trim()+"\n");
    }

}
