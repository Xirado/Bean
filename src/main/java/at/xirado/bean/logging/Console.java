package at.xirado.bean.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

public class Console
{
    public static Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
    public static void log(Level level, String s)
    {
        switch (level.levelStr.toUpperCase())
        {
            case "TRACE":
                logger.trace(s);
                return;
            case "DEBUG":
                logger.debug(s);
                return;
            case "INFO":
                logger.info(s);
                return;
            case "WARN":
                logger.warn(s);
                return;
            case "ERROR":
                logger.error(s);
                return;
            default:
                logger.error(ExceptionUtils.getStackTrace(new IllegalArgumentException("Invalid loglevel")));
        }
    }
    public static void info(String s)
    {
        logger.info(s);
    }
    public static void trace(String s)
    {
        logger.trace(s);
    }
    public static void debug(String s)
    {
        logger.debug(s);
    }
    public static void warn(String s)
    {
        logger.warn(s);
    }
    public static void error(String s)
    {
        logger.error(s);
    }
}
