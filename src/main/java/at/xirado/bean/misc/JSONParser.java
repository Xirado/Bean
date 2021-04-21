package at.xirado.bean.misc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class JSONParser
{

    private String root = null;
    private final Map<String, ?> map;

    private JSONParser(Map<String, ?> map)
    {
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    public static JSONParser parse(String jsonString)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(jsonString, Map.class);
            return new JSONParser(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JSONParser parse(URL url)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(url, Map.class);
            return new JSONParser(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JSONParser parse(File file)
    {
        try{
            if(!file.exists()) return null;
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(file, Map.class);
            return new JSONParser(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JSONParser parse(InputStream is)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(is, Map.class);
            return new JSONParser(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private Object get(Map<String, ?> map, String[] parts) {
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
        return map.get(parts[index]);
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getRoot()
    {
        return this.root;
    }

    public String getString(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof String)) return null;
        return (String) result;
    }

    public Integer getInt(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof Integer)) return null;;
        return (Integer) result;
    }

    public Boolean getBoolean(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof Boolean)) return null;
        return (Boolean) result;
    }

    public Double getDouble(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof Double)) return null;
        return (Double) result;
    }

    public Float getFloat(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof Float)) return null;
        return (Float) result;
    }

    public Long getLong(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof Long)) return null;
        return (Long) result;
    }

    public Object getObject(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        return get(map, actualQuery.split("\\."));
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> getMap(String query)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        return (Map<String, ?>) get(map, actualQuery.split("\\."));
    }

    public <T> T get(String query, Class<T> type)
    {
        String actualQuery;
        if (root == null || root.equals("")) actualQuery = query;
        else actualQuery = root + "." + query;
        Object result = get(map, actualQuery.split("\\."));
        if(result == null) return null;
        if(!type.isInstance(result)) throw new IllegalArgumentException(result.getClass().getName()+" cannot be cast to "+type.getName()+"!");
        return type.cast(result);
    }
}
