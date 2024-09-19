/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataPath;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.File;
import java.security.CodeSource;
import java.util.concurrent.ThreadFactory;

public class Util {

    public static JDA firstShard() {
        return Bean.getInstance().getShardManager().getShards().get(0);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static Button getSupportButton() {
        return Button.link(Bean.SUPPORT_GUILD_INVITE, "Support").withEmoji(Emoji.fromCustom("Bean", 922866602628743188L, false));
    }

    /**
     * Auto closes AutoClosables
     *
     * @param closeables Closeables
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void sendDM(long userId, CharSequence sequence) {
        firstShard().openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessage(sequence))
                .queue(s ->
                {
                }, e ->
                {
                });
    }

    public static void sendDM(long userId, MessageEmbed embed, MessageEmbed... embeds) {
        Checks.notNull(embed, "Embed");
        Checks.noneNull(embeds, "Embeds");
        firstShard().openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessageEmbeds(embed, embeds))
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
    public static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    public static String getJarPath() {
        try {
            CodeSource codeSource = Bean.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            return (jarFile.getParentFile().getPath());
        } catch (Exception e) {
            LOGGER.error("Could not get path of jar!", e);
            return null;
        }
    }

    public static ThreadFactory newThreadFactory(String threadName, Logger logger) {
        return newThreadFactory(threadName, logger, true);
    }

    public static ThreadFactory newThreadFactory(String threadName, Logger logger, boolean isdaemon) {
        return (r) ->
        {
            Thread t = new Thread(r, threadName);
            t.setDaemon(isdaemon);
            t.setUncaughtExceptionHandler((final Thread thread, final Throwable throwable) ->
                    logger.error("There was a uncaught exception in the {} threadpool", thread.getName(), throwable));
            return t;
        };
    }

    public static String format(String text, Object... arguments) {
        return MessageFormatter.arrayFormat(text, arguments).getMessage();
    }

    public static String getRecursive(DataObject object, String path) {
        return DataPath.getString(object, path);
    }
}
