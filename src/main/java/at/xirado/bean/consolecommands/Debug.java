package at.xirado.bean.consolecommands;

import at.xirado.bean.commandmanager.ConsoleCommand;
import at.xirado.bean.logging.Console;
import at.xirado.bean.main.DiscordBot;

import java.util.concurrent.TimeUnit;

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
