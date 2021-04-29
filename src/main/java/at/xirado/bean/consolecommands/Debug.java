package at.xirado.bean.consolecommands;

import at.xirado.bean.Bean;
import at.xirado.bean.objects.ConsoleCommand;
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


        System.out.println("Ping!");
        System.err.println("ERROR ERROR ERROR");
        Bean.instance.scheduledExecutorService.schedule(() ->
        {
            Throwable t = new ArrayIndexOutOfBoundsException("Exception text goes here blah blah");
            logger.error("Pong!", t);

        }, 1, TimeUnit.SECONDS);
    }
}
