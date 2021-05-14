package at.xirado.bean.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class JSON
{

    private String root = null;
    private final Map<String, ?> map;
    private String[] metadata = null;

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
    public static JSON parse(URL url)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(url, Map.class);
            return new JSON(map);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JSON parse(File file)
    {
        try{
            if(!file.exists()) return null;
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> map = (Map<String, ?>) mapper.readValue(file, Map.class);
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

    public static <T> T getAt(String source, String path, Class<T> type) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(source);
        return objectMapper.treeToValue(jsonNode.at(path), type);
    }

    public static <T> T getAt(URL url, String path, Class<T> type) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(url);
        return objectMapper.treeToValue(jsonNode.at(path), type);
    }

    public static <T> T getAt(File file, String path, Class<T> type) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(file);
        return objectMapper.treeToValue(jsonNode.at(path), type);
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

    public String getString(String query, Object... objects)
    {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if(!(result instanceof String)) return null;
        return String.format((String) result, objects);
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


    public String[] getMetadata()
    {
        return this.metadata;
    }

    public JSON setMetadata(String[] metadata)
    {
        this.metadata = metadata;
        return this;
    }
}
