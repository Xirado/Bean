package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Array;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ThreadFactory;

public class Util
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static Button getSupportButton()
    {
        return Button.link(Bean.SUPPORT_GUILD_INVITE, "Support").withEmoji(Emoji.fromEmote("Bean", 922866602628743188L, false));
    }

    public static Button getDontShowThisAgainButton(String hint)
    {
        return Button.primary("ackHint:"+hint, "Don't show this again");
    }

    public static int getAvailableLavalinkNodes()
    {
        return Bean.getInstance().getLavalink().getNodes().stream()
                .mapToInt(x -> x.isAvailable() ? 1 : 0)
                .sum();
    }

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
        return "[" + track.getInfo().title +"](<" + track.getInfo().uri + ">)"+(track.getInfo().author == null ? "" : " **by "+track.getInfo().author+"**");
    }

    public static String titleMarkdown(AudioTrack track, boolean withAuthor)
    {
        return "[" + track.getInfo().title +"](<" + track.getInfo().uri + ">)"+(withAuthor ? (track.getInfo().author == null ? "" : " **by "+track.getInfo().author+"**") : "");
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

    public static String format(String text, Object... arguments)
    {
        return MessageFormatter.arrayFormat(text, arguments).getMessage();
    }


    @SuppressWarnings("unchecked")
    public static <T> T[] addToArray(T[] source, T element)
    {
        T[] destination = (T[]) Array.newInstance(element.getClass(), source.length+1);
        System.arraycopy(source, 0, destination, 0, source.length);
        destination[source.length] = element;
        return destination;
    }

    public class StringUtil {

        /**
         * Copies all elements from the iterable collection of originals to the
         * collection provided.
         *
         * @param token String to search for
         * @param originals An iterable collection of strings to filter.
         * @param collection The collection to add matches to
         * @return the collection provided that would have the elements copied
         *     into
         * @throws UnsupportedOperationException if the collection is immutable
         *     and originals contains a string which starts with the specified
         *     search string.
         * @throws IllegalArgumentException if any parameter is is null
         * @throws IllegalArgumentException if originals contains a null element.
         *     <b>Note: the collection may be modified before this is thrown</b>
         */
        public static <T extends Collection<? super String>> T copyPartialMatches(final String token, final Iterable<String> originals, final T collection) throws UnsupportedOperationException, IllegalArgumentException {
            Checks.notNull(token, "Search token");
            Checks.notNull(collection, "Collection");
            Checks.notNull(originals, "Originals");

            for (String string : originals) {
                if (startsWithIgnoreCase(string, token)) {
                    collection.add(string);
                }
            }

            return collection;
        }

        /**
         * This method uses a region to check case-insensitive equality. This
         * means the internal array does not need to be copied like a
         * toLowerCase() call would.
         *
         * @param string String to check
         * @param prefix Prefix of string to compare
         * @return true if provided string starts with, ignoring case, the prefix
         *     provided
         * @throws NullPointerException if prefix is null
         * @throws IllegalArgumentException if string is null
         */
        public static boolean startsWithIgnoreCase(final String string, final String prefix) throws IllegalArgumentException, NullPointerException {
            Checks.notNull(string, "String");
            if (string.length() < prefix.length()) {
                return false;
            }
            return string.regionMatches(true, 0, prefix, 0, prefix.length());
        }
    }

    public static int getListeningUsers(@Nonnull VoiceChannel channel)
    {
        int nonBots = 0;
        for (Member member : channel.getMembers())
        {
            if (!member.getUser().isBot() && !member.getVoiceState().isDeafened())
                nonBots++;
        }
        return nonBots;
    }
}
