package at.xirado.bean.log;

import at.xirado.bean.Bean;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import club.minnced.discord.webhook.WebhookClient;
import org.jline.utils.AttributedStringBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class FileLoggerLayout extends LayoutBase<ILoggingEvent>
{

    private final List<String> pendingMessages = new ArrayList<>();
    private final ReentrantLock webhookLock = new ReentrantLock();
    private final int emptyLength = getWebhookMessageLength(Collections.emptyList());

    public FileLoggerLayout()
    {
        Thread webhookThread = new Thread(() ->
        {
            int state = 0;
            long waitingTime = 0;
            while (true)
            {
                webhookLock.lock();
                int size = pendingMessages.size();
                webhookLock.unlock();
                if (size > 0 && state != 1)
                {
                    state = 1; //Waiting
                    waitingTime = System.currentTimeMillis();
                }
                if (state == 1 && (System.currentTimeMillis() > waitingTime + 3000))
                {
                    webhookLock.lock();
                    List<List<String>> pack = splitUp(pendingMessages);
                    pack.forEach(this::sendWebhook);
                    pendingMessages.clear();
                    webhookLock.unlock();
                    state = 0;
                }
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException ignored)
                {
                }
            }
        });
        webhookThread.setDaemon(true);
        webhookThread.start();
    }

    public String doLayout(ILoggingEvent event)
    {
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
        if (event.getThrowableProxy() != null)
        {
            IThrowableProxy iThrowableProxy = event.getThrowableProxy();
            asb.append("\n").append(ThrowableProxyUtil.asString(iThrowableProxy));
        }
        asb.append(CoreConstants.LINE_SEPARATOR);
        sbuf.append(asb.toAnsi());
        if (Bean.getInstance().getWebhookClient() != null && event.getLevel() != Level.DEBUG)
        {
            webhookLock.lock();
            pendingMessages.add(getFormatted(event));
            webhookLock.unlock();
        }
        return sbuf.toString();
    }

    private static final String GREY = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String WHITE = "\u001B[37m";
    private static final String RESET = "\u001B[0m";

    private static String getFormatted(ILoggingEvent event)
    {
        String formattedMessage = event.getFormattedMessage();
        if (formattedMessage.length() > 1500)
            formattedMessage = "[!] Message too long";
        Level level = event.getLevel();
        String priColor = "";
        if (level.equals(Level.WARN))
            priColor = YELLOW;
        else if (level.equals(Level.ERROR))
            priColor = RED;
        else if (level.equals(Level.INFO))
            priColor = BLUE;
        else
            priColor = WHITE;
        StringBuilder builder = new StringBuilder()
                .append(GREY)
                .append("[")
                .append(priColor)
                .append(event.getLevel().levelStr.toUpperCase(Locale.ROOT))
                .append(GREY)
                .append("] ")
                .append(priColor)
                .append(formattedMessage)
                .append("\n");
        if (event.getThrowableProxy() != null)
        {
            IThrowableProxy iThrowableProxy = event.getThrowableProxy();
            String result = ThrowableProxyUtil.asString(iThrowableProxy).length() > 1500 ? "    [!] Stacktrace too long!\n" : ThrowableProxyUtil.asString(iThrowableProxy) + "\n";
            builder.append(result).append(RESET);
        }
        return builder.toString();
    }

    private void sendWebhook(List<String> logs)
    {
        WebhookClient client = Bean.getInstance().getWebhookClient();
        StringBuilder result = new StringBuilder("```ansi\n");
        logs.forEach(result::append);
        result.append("```");
        client.send(result.toString().trim());
        logs.clear();
    }

    private int getWebhookMessageLength(List<String> logs)
    {
        StringBuilder result = new StringBuilder("```ansi\n");
        logs.forEach(result::append);
        result.append("```");
        return result.length();
    }

    /**
     * Splits up a List of Strings into smaller Lists that are smaller than 2000 characters in length
     *
     * @param input The input list
     * @return a List of Lists with a resulting length smaller than 2000 characters
     */
    private List<List<String>> splitUp(List<String> input)
    {
        List<List<String>> output = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int currentSize = emptyLength; // Including the char count of the code block (should be 11)
        int index = 0;
        for (int i = 0; i < input.size(); i++)
        {
            if (currentSize + input.get(i).length() <= 2000)
            {
                currentSize += input.get(i).length();
                current.add(input.get(i));
                continue;
            }
            output.add(new ArrayList<>(current));
            current.clear();
            current.add(input.get(i));
            currentSize = input.get(i).length();
            index = i;
        }
        List<String> lastIter = new ArrayList<>();
        for (int i = index; i < input.size(); i++)
            lastIter.add(input.get(i));
        output.add(lastIter);
        return output;
    }
}
