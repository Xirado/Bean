package at.Xirado.Bean.ConsoleCommands;

import at.Xirado.Bean.CommandManager.ConsoleCommand;
import at.Xirado.Bean.Main.DiscordBot;

import java.util.concurrent.TimeUnit;

public class Debug extends ConsoleCommand
{
    public Debug()
    {
        this.invoke = "debug";
    }
    @Override
    public void executeCommand(String invoke, String[] args)
    {
        DiscordBot.instance.scheduledExecutorService.schedule(() ->
        {
            System.out.printf("Hello %s!\n", "there");
        }, 1, TimeUnit.SECONDS);
    }
}
