package at.xirado.bean.logging;

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

public class FileLoggerLayout extends LayoutBase<ILoggingEvent> {



    public String doLayout(ILoggingEvent event) {
        StringBuilder sbuf = new StringBuilder();
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDate = myDateObj.format(myFormatObj);
        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.append("[").append(formattedDate).append("] [")
                .append(event.getThreadName())
                .append("] [")
                .append(event.getLevel().levelStr.toUpperCase())
                .append("]: ")
                .append(event.getFormattedMessage());
        if(event.getThrowableProxy() != null)
        {
            IThrowableProxy iThrowableProxy = event.getThrowableProxy();
            asb.append("\n").append(ThrowableProxyUtil.asString(iThrowableProxy));
        }
        asb.append(CoreConstants.LINE_SEPARATOR);
        sbuf.append(asb.toAnsi());
        return sbuf.toString();
    }
}
