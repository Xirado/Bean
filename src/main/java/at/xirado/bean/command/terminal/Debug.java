package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Debug extends ConsoleCommand
{
    private static final Logger logger = LoggerFactory.getLogger(Debug.class);

    public Debug()
    {
        this.invoke = "debug";
        this.description = "Debug command";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        Bean.getInstance().getExecutor().schedule((Runnable) () ->
        {
            logger.error("An error occured", new IllegalArgumentException("Test"));
        }, 5, TimeUnit.SECONDS);
    }
}
