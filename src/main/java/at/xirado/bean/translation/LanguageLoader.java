package at.xirado.bean.translation;

import at.xirado.bean.misc.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LanguageLoader {

    private static final Logger log = LoggerFactory.getLogger(LanguageLoader.class);

    public static final List<String> LANGUAGES = new ArrayList<>();
    private static Map<String, JSON> LANGUAGE_MAP;

    static
    {
        Map<String, JSON> m = new HashMap<>();
        var mapper = new ObjectMapper();

        try (var is = LanguageLoader.class.getResourceAsStream("/assets/languages/list.txt")) {
            for (var lang : IOUtils.toString(is, StandardCharsets.UTF_8).trim().split("\n")) {
                var language = lang.trim();
                LANGUAGES.add(language);
            }
        } catch (IOException e) {
            log.error("Could not initialize Language Loader!");
            throw new ExceptionInInitializerError(e);
        }

        for (String lang : LANGUAGES) {

            try(var is = LanguageLoader.class.getResourceAsStream("/assets/languages/" + lang)) {
                var name = lang.replace(".json", "");
                JSON json = JSON.parse(is);
                if(json == null) continue;
                m.put(name, json.setMetadata(new String[]{name}));
                log.info("Initialized translation file {}", name);
            } catch (Exception e) {
                log.error("Could not initialize Language!");
                throw new ExceptionInInitializerError(e);
            }
        }

        LANGUAGE_MAP = Collections.unmodifiableMap(m);
    }

    public static String[] getLoadedLanguages()
    {
        return (String[]) LANGUAGES.toArray();
    }
    public static JSON getForLanguage(String language)
    {
        var lang = LANGUAGE_MAP.get(language);
        if (lang == null) {
            return LANGUAGE_MAP.get("en_US");
        }

        return lang;
    }

    public static JSON ofGuild(Guild guild)
    {
        Locale locale = guild.getLocale();
        String tag = locale.toLanguageTag();
        if(LANGUAGE_MAP.containsKey(tag))
        {
            return LANGUAGE_MAP.get(tag);
        }else {
            return LANGUAGE_MAP.get("en_US");
        }
    }

    public static boolean isValidLanguage(String lang) {
        return LANGUAGE_MAP.containsKey(lang);
    }
}
