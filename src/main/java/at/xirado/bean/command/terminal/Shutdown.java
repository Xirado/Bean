package at.xirado.bean.command.terminal;

import at.xirado.bean.command.ConsoleCommand;
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
        System.exit(0);
    }
}
