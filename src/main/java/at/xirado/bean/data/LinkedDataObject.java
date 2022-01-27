package at.xirado.bean.data;

import at.xirado.bean.misc.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedDataObject {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String root = null;
    private final Map<String, Object> map;
    private String[] metadata = null;

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected LinkedDataObject(Map<String, Object> map) {
        this.map = map;

    }


    @SuppressWarnings("unchecked")
    public static LinkedDataObject parse(String jsonString) {
        try {
            Map<String, Object> map = (Map<String, Object>) MAPPER.readValue(jsonString, LinkedHashMap.class);
            return new LinkedDataObject(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public LinkedDataObject configureMapper(DeserializationFeature feature, boolean value) {
        MAPPER.configure(feature, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public static LinkedDataObject parse(URL url) {
        try {
            Map<String, Object> map = (Map<String, Object>) MAPPER.readValue(url, LinkedHashMap.class);
            return new LinkedDataObject(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static LinkedDataObject parse(File file) {
        try {
            if (!file.exists()) return null;
            Map<String, Object> map = (Map<String, Object>) MAPPER.readValue(file, LinkedHashMap.class);
            return new LinkedDataObject(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static LinkedDataObject parse(InputStream is) {
        if (is == null) throw new IllegalArgumentException("InputStream is null!");
        try {
            Map<String, Object> map = (Map<String, Object>) MAPPER.readValue(is, LinkedHashMap.class);
            return new LinkedDataObject(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static LinkedDataObject empty() {
        return new LinkedDataObject(new LinkedHashMap<>());
    }

    public <T> T convertValueAt(String path, Class<T> type) {
        Object value = getObject(path);
        return MAPPER.convertValue(value, type);
    }

    public LinkedDataObject put(String key, Object object) {
        map.put(key, object);
        return this;
    }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public String toPrettyString() throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    }

    public LinkedDataObject putNull(String key) {
        map.put(key, null);
        return this;
    }

    @SuppressWarnings("unchecked")
    private Object get(Map<String, Object> map, String[] parts) {
        var index = 0;
        while (index != parts.length - 1) {
            Object maybeMap = map.get(parts[index]);
            if (maybeMap instanceof Map) {
                map = (Map<String, Object>) maybeMap;
                index++;
            } else {
                return null;
            }
        }
        return map.get(parts[index]);
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return this.root;
    }

    public String getString(String query, Object... objects) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if (!(result instanceof String)) return null;
        return Util.format((String) result, objects);
    }

    public Integer getInt(String query) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if (!(result instanceof Integer)) return null;
        return (Integer) result;
    }

    public Boolean getBoolean(String query) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if (!(result instanceof Boolean)) return null;
        return (Boolean) result;
    }

    public Double getDouble(String query) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if (!(result instanceof Double)) return null;
        return (Double) result;
    }

    public Float getFloat(String query) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if (!(result instanceof Float)) return null;
        return (Float) result;
    }

    public Long getLong(String query) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        Object result = get(map, actualQuery.split("\\."));
        if (!(result instanceof Long)) return null;
        return (Long) result;
    }

    public Object getObject(String query) {
        String actualQuery;

        if (root == null || root.equals("")) {
            actualQuery = query;
        } else {
            actualQuery = root + "." + query;
        }
        return get(map, actualQuery.split("\\."));
    }

    public <T> T get(String query, Class<T> type) {
        String actualQuery;
        if (root == null || root.equals("")) actualQuery = query;
        else actualQuery = root + "." + query;
        Object result = get(map, actualQuery.split("\\."));
        if (result == null) return null;
        if (!type.isInstance(result))
            throw new IllegalArgumentException(result.getClass().getName() + " cannot be cast to " + type.getName() + "!");
        return type.cast(result);
    }

    public String[] getMetadata() {
        return this.metadata;
    }

    public LinkedDataObject setMetadata(String[] metadata) {
        this.metadata = metadata;
        return this;
    }
}
