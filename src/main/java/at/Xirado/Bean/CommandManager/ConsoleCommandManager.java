package at.Xirado.Bean.CommandManager;

import at.Xirado.Bean.ConsoleCommands.*;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
public class ConsoleCommandManager
{
    public final ArrayList<ConsoleCommand> consoleCommands = new ArrayList<>();

    public void handleConsoleCommand(final String invoke, final String[] args)
    {
         Runnable r = () ->
         {
             try
             {
                 boolean foundCommand = false;
                 for(ConsoleCommand ccmd : consoleCommands)
                 {
                     if(ccmd.getInvoke().equalsIgnoreCase(invoke) || ccmd.getAliases().stream().anyMatch(invoke::equalsIgnoreCase))
                     {
                         ccmd.executeCommand(invoke, args);
                         foundCommand = true;
                         break;
                     }
                 }
                 if(!foundCommand) System.out.println(ansi().fg(RED).a("Unbekannter Befehl \""+invoke+"\""));
             }catch(Exception e)
             {
                 Console.logger.error("Could not execute console-command", e);
             }
         };
        DiscordBot.instance.scheduledExecutorService.submit(r);
    }
    public void registerCommand(ConsoleCommand ccmd)
    {
        consoleCommands.add(ccmd);
    }
    public void registerAllCommands()
    {
        registerCommand(new SendMessage());
        registerCommand(new LogLevel());
        registerCommand(new Clearscreen());
        registerCommand(new Shutdown());
        registerCommand(new Debug());
        registerCommand(new Info());
        registerCommand(new Echo());
        registerCommand(new Help());
    }
}
