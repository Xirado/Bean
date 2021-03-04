package at.xirado.bean.consolecommands;

import at.xirado.bean.commandmanager.ConsoleCommand;
import at.xirado.bean.misc.Util;
import ch.qos.logback.classic.Level;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;
public class LogLevel extends ConsoleCommand
{
    public LogLevel()
    {
        this.invoke = "loglevel";
        this.description = "Changes the loglevel";

    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if(args.length == 0)
        {
            System.out.println(ansi().fg(RED).a("Mögliche Optionen sind: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL"));
            return;
        }
        switch (args[0].toUpperCase())
        {
            case "OFF":
                Util.setLoggingLevel(Level.OFF);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"OFF\" geändert"));
                break;
            case "FATAL":
                Util.setLoggingLevel(Level.toLevel(100));
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"FATAL\" geändert"));
                break;
            case "ERROR":
                Util.setLoggingLevel(Level.ERROR);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"ERROR\" geändert"));
                break;
            case "WARN":
                Util.setLoggingLevel(Level.WARN);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"WARN\" geändert"));
                break;
            case "INFO":
                Util.setLoggingLevel(Level.INFO);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"INFO\" geändert"));
                break;
            case "DEBUG":
                Util.setLoggingLevel(Level.DEBUG);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"DEBUG\" geändert"));
                break;
            case "TRACE":
                Util.setLoggingLevel(Level.TRACE);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"TRACE\" geändert"));
                break;
            case "ALL":
                Util.setLoggingLevel(Level.ALL);
                System.out.println(ansi().fg(GREEN).a("LogLevel wurde auf \"ALL\" geändert"));
                break;
            default:
                System.out.println(ansi().fg(RED).a("Mögliche Optionen sind: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL"));

        }
    }
}
