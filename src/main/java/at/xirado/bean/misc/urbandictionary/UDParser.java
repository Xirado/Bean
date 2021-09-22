package at.xirado.bean.misc.urbandictionary;

import at.xirado.bean.data.DataObject;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class UDParser
{
    private final String apiUri;

    /**
     * Initialize a new instance of a UDParser object using the URL for the UrbanDictionary API page.
     * Examples for the API URL are "http://api.urbandictionary.com/v0/".
     *
     * @param apiUri
     */
    public UDParser(String apiUri)
    {
        this.apiUri = apiUri;
    }

    public UrbanDefinition[] getDefinitionsWithJSONData(String jsonData)
    {
        return DataObject.parse(jsonData).configureMapper(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).convertValueAt("list", UrbanDefinition[].class);
    }


    /**
     * Return a String containing data in JSON format about a specified word.
     * Words inputted that contain spaces must be formatted in the following format:
     * "windows xp" -> "windows+xp"
     *
     * @param word
     * @return a JSON-formatted String containing information on the word.
     */
    public String getJSONData(String word)
    {
        try
        {
            // if the word contains spaces, replace them with "+" in the GET.
            String url_base = apiUri + "define?term=" + word;
            URL jsonURL = new URL(url_base);
            URLConnection yc = jsonURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder returnData = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
            {
                returnData.append(inputLine);
            }
            return returnData.toString();
        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Return a String containing data in JSON format about a specified definition id.
     *
     * @param id (the defid of the word)
     * @return a JSON-formatted String containing information on the word.
     */
    public String getJSONData(int id)
    {
        try
        {
            // if the word contains spaces, replace them with "+" in the GET.
            String url_base = apiUri + "define?defid=" + id;
            URL jsonURL = new URL(url_base);
            URLConnection yc = jsonURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder returnData = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
            {
                returnData.append(inputLine);
            }
            return returnData.toString();
        } catch (Exception e)
        {
            return null;
        }
    }

}
