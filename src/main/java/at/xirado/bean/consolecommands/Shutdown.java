package at.xirado.bean.consolecommands;

import at.xirado.bean.commandmanager.ConsoleCommand;
import at.xirado.bean.logging.Console;
import at.xirado.bean.main.DiscordBot;

public class Shutdown extends ConsoleCommand
{
    public Shutdown()
    {
        this.invoke = "shutdown";
        this.description = "Shuts down all JDA instances and all threadpools";
    }


    @Override
    public void executeCommand(String invoke, String[] args)
    {
        Console.info("Shutting down...");
        DiscordBot.getInstance().scheduledExecutorService.shutdown();
        DiscordBot.getInstance().jda.shutdown();
    }
}
