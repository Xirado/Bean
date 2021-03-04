package at.Xirado.Bean.ConsoleCommands;

import at.Xirado.Bean.CommandManager.ConsoleCommand;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;

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
