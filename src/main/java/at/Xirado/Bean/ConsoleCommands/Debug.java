package at.Xirado.Bean.ConsoleCommands;

import at.Xirado.Bean.CommandManager.ConsoleCommand;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Debug extends ConsoleCommand
{
    public Debug()
    {
        this.invoke = "debug";
        this.description = "Debug command";
    }
    @Override
    public void executeCommand(String invoke, String[] args)
    {
        System.out.println("Ping!");
        System.err.println("ERROR ERROR ERROR");
        DiscordBot.instance.scheduledExecutorService.schedule(() ->
        {
            Throwable t = new ArrayIndexOutOfBoundsException("Exception text goes here blah blah");
            Console.logger.error("Pong!", t);

        }, 1, TimeUnit.SECONDS);
    }
}
