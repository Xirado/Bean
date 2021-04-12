package at.xirado.bean.misc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class JSON
{

    private static final ThreadLocal<String> ROOT = new ThreadLocal<>();
    private final Map<String, ?> map;

    private JSON(Map<String, ?> map)
    {
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    public static JSON parse(String jsonString)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(jsonString, Map.class);
            return new JSON(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JSON parse(InputStream is)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(is, Map.class);
            return new JSON(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
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
                return null;
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
        return null;
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

    public String get(String query, Object... objects) {
        var root = ROOT.get();
        String actualQuery;

        if (root == null) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }

        String result = String.format(get(map, actualQuery.split("\\."), false), objects);
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
