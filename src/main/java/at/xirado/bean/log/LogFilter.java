package at.xirado.bean.log;

import at.xirado.bean.Bean;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class LogFilter extends Filter<ILoggingEvent>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);
    private static final Set<Class<?>> FILTERED_CLASSES = new HashSet<>();

    @Override
    public FilterReply decide(final ILoggingEvent event)
    {
        if (!event.getLevel().isGreaterOrEqual(Level.INFO))
            return FilterReply.DENY;
        final IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null)
        {
            return FilterReply.NEUTRAL;
        }

        if (!(throwableProxy instanceof ThrowableProxy))
        {
            return FilterReply.NEUTRAL;
        }

        final ThrowableProxy throwableProxyImpl =
                (ThrowableProxy) throwableProxy;
        final Throwable throwable = throwableProxyImpl.getThrowable();
        if (FILTERED_CLASSES.stream().anyMatch(x -> x.isInstance(throwable)))
        {
            Bean.error("Redacted error (" + throwable.getClass().getName() + ")");
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }

    public static void addFilteredClass(String exceptionClassName)
    {
        try
        {
            FILTERED_CLASSES.add(Class.forName(exceptionClassName));
        } catch (final ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Class is unavailable: "
                    + exceptionClassName, e);
        }
    }

    public static void addFilteredClass(Class<?> clazz)
    {
        FILTERED_CLASSES.add(clazz);
    }
}
