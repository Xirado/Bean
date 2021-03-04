package at.xirado.bean.misc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class JSONConfig
{

	public static Gson gson = new Gson();
	public static Long channel;
	public static final Map<String, String> config = new HashMap<String, String>();
	public static void updateConfig()
	{
		config.clear();
		List<Map<String,String>> list = getConfig("config.json");
		for(int i = 0; i < list.size(); i++)
		{
			Map<String,String> current = list.get(i);
			Iterator it = current.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        config.put(String.valueOf(pair.getKey()), String.valueOf(pair.getValue()));	        
		        it.remove();
		    }
		    
		}
	}
	public static Long getLogChannel()
	{
		JSONParser parser = new  JSONParser();
		try(FileReader reader = new FileReader("config.json"))
		{
			Object obj = parser.parse(reader);
			JSONArray arr = (JSONArray) obj;
			HashMap<Object,Object> values = new HashMap();
			arr.forEach(conf -> parse((JSONObject)conf, "LogChannel"));
			return channel;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void parse(JSONObject Object, Object value)
	{
		channel = (Long)Object.get("LogChannel");
	}
	
	public static List<Map<String,String>> getConfig(String filename)
	{
		try(FileReader reader = new FileReader(filename))
		{
			
			Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			return gson.fromJson(bufferedReader, type);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}

	public static JSONArray getConfig1(String filename)
	{
		JSONParser parser = new JSONParser();
		try(FileReader reader = new FileReader(filename))
		{
			Object obj = parser.parse(reader);
			return (JSONArray)obj;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
