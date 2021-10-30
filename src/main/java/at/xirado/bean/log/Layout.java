package at.xirado.bean.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Layout extends LayoutBase<ILoggingEvent>
{

    private static final AttributedStyle PRIMARY =   AttributedStyle.DEFAULT.foreground(25, 140, 255);
    private static final AttributedStyle SECONDARY = AttributedStyle.DEFAULT.foreground(85, 85, 85);
    private static final AttributedStyle FATAL =     AttributedStyle.DEFAULT.foreground(222, 23, 56);
    private static final AttributedStyle WARN =      AttributedStyle.DEFAULT.foreground(255, 255, 0);

    @Override
    public String doLayout(ILoggingEvent event)
    {
        StringBuilder sBuilder = new StringBuilder();
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss");
        String formattedDate = myDateObj.format(myFormatObj);
        AttributedStringBuilder asb = new AttributedStringBuilder();
        AttributedStyle priColour;
        Level level = event.getLevel();
        if (Level.ERROR.equals(level))
            priColour = FATAL;
        else if (Level.WARN.equals(level))
            priColour = WARN;
        else
            priColour = PRIMARY;
        asb.style(SECONDARY).append("[").style(priColour).append(formattedDate).style(SECONDARY).append("] [")
                .style(priColour).append(event.getThreadName()).style(SECONDARY).append("] [").style(priColour)
                .append(event.getLevel().levelStr.toUpperCase()).style(SECONDARY).append("]: ").style(priColour)
                .append(event.getFormattedMessage());
        if (event.getThrowableProxy() != null)
        {
            IThrowableProxy iThrowableProxy = event.getThrowableProxy();
            asb.append("\n").style(priColour).append(ThrowableProxyUtil.asString(iThrowableProxy));
        }
        asb.style(AttributedStyle.DEFAULT).append(CoreConstants.LINE_SEPARATOR);
        sBuilder.append(asb.toAnsi());
        return sBuilder.toString();
    }
}
