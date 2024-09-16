package at.xirado.bean.translation;

import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LocaleLoader {

    private static final Logger log = LoggerFactory.getLogger(LocaleLoader.class);

    public static final List<String> LANGUAGES = new ArrayList<>();
    private static final Map<String, DataObject> LANGUAGE_MAP;

    static {
        Map<String, DataObject> m = new HashMap<>();

        try (var is = LocaleLoader.class.getResourceAsStream("/assets/languages/list.txt")) {
            if (is == null) {
                throw new ExceptionInInitializerError("Could not initialize Language loader because list.txt does not exist!");
            }
            for (var lang : new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().toList()) {
                var language = lang.trim();
                LANGUAGES.add(language);
            }
        } catch (IOException e) {
            log.error("Could not initialize locale loader!");
            throw new ExceptionInInitializerError(e);
        }

        for (String lang : LANGUAGES) {
            try (var is = LocaleLoader.class.getResourceAsStream("/assets/languages/" + lang)) {
                if (is == null) {
                    log.error("Locale {} does not exist!", lang);
                    continue;
                }
                var name = lang.replace(".json", "");
                DataObject json = DataObject.fromJson(is);
                m.put(name, json);
                log.info("Successfully loaded locale {}", lang);
            } catch (Exception e) {
                log.error("Could not load locale '{}'!", lang, e);
            }
        }

        LANGUAGE_MAP = Collections.unmodifiableMap(m);
    }

    public static DataObject getForLanguage(String language) {
        var lang = LANGUAGE_MAP.get(language);
        if (lang == null) {
            return LANGUAGE_MAP.get("en_US");
        }

        return lang;
    }

    public static DataObject ofGuild(Guild guild) {
        DiscordLocale locale = guild.getLocale();
        String tag = locale.getLocale();
        if (LANGUAGE_MAP.containsKey(tag)) {
            return LANGUAGE_MAP.get(tag);
        } else {
            return LANGUAGE_MAP.get("en_US");
        }
    }

    public static boolean isValidLanguage(String lang) {
        return LANGUAGE_MAP.containsKey(lang);
    }


    public static String parseDuration(long seconds, DataObject languageJSON, String delimiter) {
        if (seconds == -1) {
            return Util.getRecursive(languageJSON, "time.permanent");
        }
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long seconds1 = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        StringBuilder ges = new StringBuilder();
        if (days != 0) {
            if (days == 1) {
                ges.append(days).append(" ").append(Util.getRecursive(languageJSON, "time.day")).append(delimiter);
            } else {
                ges.append(days).append(" ").append(Util.getRecursive(languageJSON, "time.days")).append(delimiter);
            }
        }
        if (hours != 0) {
            if (hours == 1) {
                ges.append(hours).append(" ").append(Util.getRecursive(languageJSON, "time.hour")).append(delimiter);
            } else {
                ges.append(hours).append(" ").append(Util.getRecursive(languageJSON, "time.hours")).append(delimiter);
            }
        }
        if (minutes != 0) {
            if (minutes == 1) {
                ges.append(minutes).append(" ").append(Util.getRecursive(languageJSON, "time.minute")).append(delimiter);
            } else {
                ges.append(minutes).append(" ").append(Util.getRecursive(languageJSON, "time.minutes")).append(delimiter);
            }
        }
        if (seconds1 != 0) {
            if (seconds1 == 1) {
                ges.append(seconds1).append(" ").append(Util.getRecursive(languageJSON, "time.second")).append(delimiter);
            } else {
                ges.append(seconds1).append(" ").append(Util.getRecursive(languageJSON, "time.seconds")).append(delimiter);
            }
        }
        String result = ges.toString();
        return result.substring(0, result.length() - delimiter.length());
    }
}
