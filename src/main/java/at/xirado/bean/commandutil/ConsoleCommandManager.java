package at.xirado.bean.commandutil;

import at.xirado.bean.Bean;
import at.xirado.bean.consolecommands.Shutdown;
import at.xirado.bean.consolecommands.*;
import at.xirado.bean.objects.ConsoleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
public class ConsoleCommandManager
{
    private static final Logger logger = LoggerFactory.getLogger(ConsoleCommandManager.class);
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
                 logger.error("Could not execute console-command", e);
             }
         };
        Bean.instance.scheduledExecutorService.submit(r);
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