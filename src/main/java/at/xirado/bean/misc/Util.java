package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadFactory;

public class Util
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /**
     * Auto closes AutoClosables
     *
     * @param closeables Closeables
     */
    public static void closeQuietly(AutoCloseable... closeables)
    {
        for (AutoCloseable c : closeables)
        {
            if (c != null)
            {
                try
                {
                    c.close();
                } catch (Exception ignored)
                {
                }
            }
        }
    }

    public static String titleMarkdown(AudioTrack track)
    {
        return "[" + track.getInfo().title + "](<" + track.getInfo().uri + ">)";
    }

    public static String timeFormat(long seconds)
    {
        return (new SimpleDateFormat("HH:mm:ss")).format(new Date(seconds));
    }

    public static void sendPM(User user, Message message)
    {
        user.openPrivateChannel()
                .flatMap(x -> x.sendMessage(message))
                .queue(s ->
                {
                }, e ->
                {
                });
    }

    public static void sendPM(User user, CharSequence sequence)
    {
        user.openPrivateChannel()
                .flatMap(x -> x.sendMessage(sequence))
                .queue(s ->
                {
                }, e ->
                {
                });
    }

    public static void sendPM(User user, MessageEmbed embed)
    {
        user.openPrivateChannel()
                .flatMap(x -> x.sendMessage(embed))
                .queue(s ->
                {
                }, e ->
                {
                });
    }

    /**
     * Makes any number ordinal
     * e.g. 2 -> 2nd
     *
     * @param i Number to format
     * @return Ordinal number
     */
    public static String ordinal(int i)
    {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        return switch (i % 100)
                {
                    case 11, 12, 13 -> i + "th";
                    default -> i + suffixes[i % 10];
                };
    }

    public static String getJarPath()
    {
        try
        {
            CodeSource codeSource = Bean.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            return (jarFile.getParentFile().getPath());
        } catch (Exception e)
        {
            LOGGER.error("Could not get path of jar!", e);
            return null;
        }
    }

    public static ThreadFactory newThreadFactory(String threadName)
    {
        return newThreadFactory(threadName, LoggerFactory.getLogger(Bean.class));
    }

    public static ThreadFactory newThreadFactory(String threadName, boolean isDaemon)
    {
        return newThreadFactory(threadName, LoggerFactory.getLogger(Bean.class), isDaemon);
    }

    public static ThreadFactory newThreadFactory(String threadName, Logger logger)
    {
        return newThreadFactory(threadName, logger, true);
    }

    public static ThreadFactory newThreadFactory(String threadName, Logger logger, boolean isdaemon)
    {
        return (r) ->
        {
            Thread t = new Thread(r, threadName);
            t.setDaemon(isdaemon);
            t.setUncaughtExceptionHandler((final Thread thread, final Throwable throwable) ->
                    logger.error("There was a uncaught exception in the {} threadpool", thread.getName(), throwable));
            return t;
        };
    }

    public static String replaceLast(final String text, final String regex, final String replacement)
    {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}
