package at.xirado.bean.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.LayoutBase
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsoleLoggingLayout : LayoutBase<ILoggingEvent>() {

    companion object {
        private val INFO = AttributedStyle.DEFAULT.foreground(25, 140, 255)
        private val ERROR = AttributedStyle.DEFAULT.foreground(222, 23, 56)
        private val WARN = AttributedStyle.DEFAULT.foreground(255, 255, 0)
        private val DEBUG = AttributedStyle.DEFAULT.foreground(159, 160, 164)
        private val SECONDARY = AttributedStyle.DEFAULT.foreground(85, 85, 85)
        private val FORMATTER = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss")
    }

    override fun doLayout(event: ILoggingEvent): String {
        val stringBuilder = AttributedStringBuilder()
        val dateTime = LocalDateTime.now()
        val date = dateTime.format(FORMATTER)

        val primaryColor = when (event.level) {
            Level.INFO -> INFO
            Level.ERROR -> ERROR
            Level.WARN -> WARN
            else -> DEBUG
        }

        stringBuilder.style(SECONDARY).append("[").style(primaryColor).append(date).style(SECONDARY).append("] [")
            .style(primaryColor).append(event.threadName).style(SECONDARY).append("] [").style(primaryColor).append(event.level.levelStr.uppercase())
            .style(SECONDARY).append("]: ").style(primaryColor).append(event.formattedMessage)

        if (event.throwableProxy != null) {
            val proxy = event.throwableProxy
            stringBuilder.append(CoreConstants.LINE_SEPARATOR).style(primaryColor).append(ThrowableProxyUtil.asString(proxy))
        }

        stringBuilder.style(AttributedStyle.DEFAULT).append(CoreConstants.LINE_SEPARATOR)

        return stringBuilder.toAnsi()
    }
}