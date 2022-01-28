package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
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
import java.util.Date;
import java.util.concurrent.ThreadFactory;

public class Util
{

    public static JDA firstShard()
    {
        return Bean.getInstance().getShardManager().getShards().get(0);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static Button getSupportButton()
    {
        return Button.link(Bean.SUPPORT_GUILD_INVITE, "Support").withEmoji(Emoji.fromEmote("Bean", 922866602628743188L, false));
    }

    public static Button getDontShowThisAgainButton(String hint)
    {
        return Button.secondary("ackHint:" + hint, "Don't show this again");
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
        return "[" + track.getInfo().title + "](<" + track.getInfo().uri + ">)" + (track.getInfo().author == null ? "" : " **by " + track.getInfo().author + "**");
    }

    public static String titleMarkdown(AudioTrack track, boolean withAuthor)
    {
        return "[" + track.getInfo().title + "](<" + track.getInfo().uri + ">)" + (withAuthor ? (track.getInfo().author == null ? "" : " **by " + track.getInfo().author + "**") : "");
    }

    public static String timeFormat(long seconds)
    {
        return (new SimpleDateFormat("HH:mm:ss")).format(new Date(seconds));
    }

    public static void sendDM(long userId, CharSequence sequence)
    {
        firstShard().openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessage(sequence))
                .queue(s -> {
                }, e -> {
                });
    }

    public static void sendDM(long userId, MessageEmbed embed, MessageEmbed... embeds)
    {
        Checks.notNull(embed, "Embed");
        Checks.noneNull(embeds, "Embeds");
        firstShard().openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessageEmbeds(embed, embeds))
                .queue(s -> {
                }, e -> {
                });

    }

    public static void sendOwnerDM(CharSequence sequence)
    {
        firstShard().openPrivateChannelById(Bean.OWNER_ID)
                .flatMap(channel -> channel.sendMessage(sequence))
                .queue(s -> {
                }, e -> {
                });
    }

    public static void sendOwnerDM(MessageEmbed embed, MessageEmbed... embeds)
    {
        Checks.notNull(embed, "Embed");
        Checks.noneNull(embeds, "Embeds");
        firstShard().openPrivateChannelById(Bean.OWNER_ID)
                .flatMap(channel -> channel.sendMessageEmbeds(embed, embeds))
                .queue(s -> {
                }, e -> {
                });
    }

    public static int zeroIfNegative(int x)
    {
        return Math.max(x, 0);
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
        T[] destination = (T[]) Array.newInstance(element.getClass(), source.length + 1);
        System.arraycopy(source, 0, destination, 0, source.length);
        destination[source.length] = element;
        return destination;
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
