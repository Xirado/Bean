package at.xirado.bean.command;

import at.xirado.bean.Bean;
import at.xirado.bean.command.terminal.Shutdown;
import at.xirado.bean.command.terminal.*;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class ConsoleCommandManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleCommandManager.class);

    private final ArrayList<ConsoleCommand> consoleCommands = new ArrayList<>();

    public void handleConsoleCommand(final String invoke, final String[] args)
    {
        Runnable r = () ->
        {
            try
            {
                boolean foundCommand = false;
                for (ConsoleCommand ccmd : consoleCommands)
                {
                    if (ccmd.getInvoke().equalsIgnoreCase(invoke) || ccmd.getAliases().stream().anyMatch(invoke::equalsIgnoreCase))
                    {
                        ccmd.executeCommand(invoke, args);
                        foundCommand = true;
                        break;
                    }
                }
                if (!foundCommand)
                    System.out.println(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xFF0000)).append("Befehl \"").append(invoke).append("\" wurde nicht gefunden!"));
            } catch (Exception e)
            {
                LOGGER.error("Could not execute console-command", e);
            }
        };
        Bean.getInstance().getExecutor().submit(r);
    }

    public void registerCommand(ConsoleCommand ccmd)
    {
        consoleCommands.add(ccmd);
    }

    public void registerAllCommands()
    {
        registerCommand(new SendMessage());
        registerCommand(new ClearScreen());
        registerCommand(new Shutdown());
        registerCommand(new Info());
        registerCommand(new Help());
        registerCommand(new PrintGuildData());
        registerCommand(new UpdateSlashCommands());
        registerCommand(new BalanceCommand());
        registerCommand(new BanGuildCommand());
        registerCommand(new ReloadConfig());
    }

    public ArrayList<ConsoleCommand> getCommands()
    {
        return consoleCommands;
    }
}
