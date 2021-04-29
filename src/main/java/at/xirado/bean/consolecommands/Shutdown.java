package at.xirado.bean.consolecommands;

import at.xirado.bean.Bean;
import at.xirado.bean.objects.ConsoleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shutdown extends ConsoleCommand
{

    private static final Logger logger = LoggerFactory.getLogger(Shutdown.class);

    public Shutdown()
    {
        this.invoke = "shutdown";
        this.description = "Shuts down all JDA instances and all threadpools";
    }


    @Override
    public void executeCommand(String invoke, String[] args)
    {
        logger.info("Shutting down...");
        Bean.getInstance().scheduledExecutorService.shutdown();
        Bean.getInstance().jda.shutdown();
        System.exit(0);
    }
}
