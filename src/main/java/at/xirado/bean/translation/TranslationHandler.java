package at.xirado.bean.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TranslationHandler
{
    private static final Logger log = LoggerFactory.getLogger(TranslationHandler.class);

    public static final List<String> LANGUAGES = new ArrayList<>();
    private static final ThreadLocal<String> ROOT = new ThreadLocal<>();
    private static Map<String, TranslationHandler> LANGUAGE_MAP;

    public static void init()
    {
        Map<String, TranslationHandler> m = new HashMap<>();
        var mapper = new ObjectMapper();

        try (var is = TranslationHandler.class.getResourceAsStream("/assets/languages/list.txt")) {
            for (var lang : IOUtils.toString(is, StandardCharsets.UTF_8).trim().split("\n")) {
                var language = lang.trim();
                LANGUAGES.add(language);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        for (String lang : LANGUAGES) {

            try(var is = TranslationHandler.class.getResourceAsStream("/assets/languages/" + lang)) {
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>) mapper.readValue(is, Map.class);

                var name = lang.replace(".json", "");
                m.put(name, new TranslationHandler(map, lang));

                log.info("Initialized I18n for: {}", name);
            } catch (Exception e) {
                throw new Error("Unable to initialize I18n", e);
            }
        }

        LANGUAGE_MAP = Collections.unmodifiableMap(m);
    }

    private final Map<String, ?> map;
    private final String language;

    private TranslationHandler(Map<String, ?> map, String language) {
        this.map = map;
        this.language = language;
    }

    public static TranslationHandler getForLanguage(String language)
    {
        var lang = LANGUAGE_MAP.get(language);
        if (lang == null) {
            return LANGUAGE_MAP.get("en_US");
        }

        return lang;
    }

    public static TranslationHandler ofGuild(Guild guild)
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

    public static void root(String newRoot) {
        ROOT.set(newRoot);
    }

    @SuppressWarnings("unchecked")
    private String get(Map<String, ?> map, String[] parts, boolean recursion) {
        var index = 0;
        while (index != parts.length - 1) {
            Object maybeMap = map.get(parts[index]);
            if (maybeMap instanceof Map) {
                map = (Map<String, ?>) maybeMap;
                index++;
            } else {
                if (language.equals("en_US") || recursion) {
                    throw new IllegalArgumentException("Missing i18n key " + String.join(".", parts));
                }

                return get(LANGUAGE_MAP.get("en_US").map, parts, true);
            }
        }

        Object maybeString = map.get(parts[index]);
        if (maybeString instanceof String) {
            return (String) maybeString;
        }

        if (maybeString instanceof Collection) {
            Collection<String> c = ((Collection<String>) maybeString);
            return c.stream()
                    .skip(ThreadLocalRandom.current().nextInt(c.size()))
                    .findFirst()
                    .orElseThrow(AssertionError::new);
        }

        if (language.equals("en_US") || recursion) {
            throw new IllegalArgumentException("Missing i18n key " + String.join(".", parts));
        }

        return get(LANGUAGE_MAP.get("en_US").map, parts, true);
    }

    public String get(String query) {
        var root = ROOT.get();
        String actualQuery;

        if (root == null) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }

        String result = get(map, actualQuery.split("\\."), false);
        return result == null ? query : result;
    }

    public String withRoot(String root, String query) {
        var s = ROOT.get();
        ROOT.set(root);

        try {
            return get(query);
        } finally {
            ROOT.set(s);
        }
    }
}