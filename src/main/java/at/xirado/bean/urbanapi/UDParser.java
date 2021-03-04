package at.xirado.bean.urbanapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class UDParser
{
    private String apiUri;

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

    public Definition[] getDefinitionsWithJSONData(String JSONData)
    {
        ArrayList<Definition> tmpArr = new ArrayList<Definition>();

        JSONObject obj = new JSONObject(JSONData);

        JSONArray wordList = obj.getJSONArray("list");

        for(int i = 0; i < wordList.length(); i++)
        {
            JSONObject jsonobject = wordList.getJSONObject(i);
            String wordName = jsonobject.getString("word");
            String definition = jsonobject.getString("definition");
            String example = jsonobject.getString("example");
            String author = jsonobject.getString("author");
            String writtenDate = jsonobject.getString("written_on");
            String permaLink = jsonobject.getString("permalink");
            int likes = (jsonobject.getInt("thumbs_up"));
            int dislikes = (jsonobject.getInt("thumbs_down"));
            int refID = (jsonobject.getInt("defid"));
            tmpArr.add(new Definition(wordName, definition, example, author, writtenDate, permaLink, refID, likes, dislikes));
        }

        return tmpArr.toArray(new Definition[tmpArr.size()]);
    }

    /**
     * Return the tags that are relevant to a keyword given the scraped JSON Data for that word.
     *
     * @param JSONData
     * @return the result type for a keyword.
     */
    public JSONArray getTagsWithJSONData(String JSONData)
    {
        JSONObject obj = new JSONObject(JSONData);
        JSONArray tags = obj.getJSONArray("tags");
        return tags;
    }

    /**
     * Return the result status of a keyword given the scraped JSON Data for that word.
     * If false, no current results exist for that keyword.
     * If true, results exist for that keyword.
     *
     * @param JSONData
     * @return the result type for a keyword.
     */
    public boolean getResultTypeByKeyword(String JSONData)
    {
        JSONObject obj = new JSONObject(JSONData);
        String result_type = obj.getString("result_type");
        return result_type.matches("exact");
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
            String url_base = apiUri + "define?term="+ word;
            URL jsonURL = new URL(url_base);
            URLConnection yc = jsonURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
            String inputLine;
            String returnData = "";
            while ((inputLine = in.readLine()) != null)
            {
                returnData += inputLine;
            }
            return returnData;
        }
        catch (Exception e)
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
            String url_base = apiUri + "define?defid="+ id;
            URL jsonURL = new URL(url_base);
            URLConnection yc = jsonURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
            String inputLine;
            String returnData = "";
            while ((inputLine = in.readLine()) != null)
            {
                returnData += inputLine;
            }
            return returnData;
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
