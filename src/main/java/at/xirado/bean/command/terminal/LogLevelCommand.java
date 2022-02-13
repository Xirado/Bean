package at.xirado.bean.command.terminal;

import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.log.Shell;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class LogLevelCommand extends ConsoleCommand
{
    private static final Logger LOG = (Logger) LoggerFactory.getLogger("ROOT");

    public LogLevelCommand()
    {
        this.invoke = "loglevel";
        this.description = "Changes the ROOT log-level during runtime";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if (args.length == 0)
            return;

        String levelString = args[0];
        Level level = Level.toLevel(levelString, Level.INFO);

        LOG.setLevel(level);

        Shell.println("Log-level has been updated to "+level);
    }
}
