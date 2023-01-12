package at.xirado.bean.misc

import at.xirado.bean.Bean
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.http.client.utils.URIBuilder
import java.net.URL

object YoutubeUtils {
    private var innertubeRequestBody = "{\"context\":{\"client\":{\"deviceMake\":\"\",\"deviceModel\":\"\",\"userAgent\":\"Mozilla/5.0\",\"clientName\":\"WEB_REMIX\",\"clientVersion\":\"1.20220330.01.00\",\"osName\":\"Windows\",\"osVersion\":\"10.0\",\"originalUrl\":\"https://music.youtube.com/\"}}}"

    fun getYoutubeMusicSearchResults(query: String) : List<String> {
        val httpClient = Bean.getInstance().okHttpClient

        val uri = URIBuilder()
        uri.scheme = "https"
        uri.host = "music.youtube.com"
        uri.path = "/youtubei/v1/music/get_search_suggestions"

        val innertubeBody = DataObject.fromJson(innertubeRequestBody)
        innertubeBody.put("input", query)

        val requestBody = innertubeBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(URL(uri.toString()))
            .post(requestBody)
            .addHeader("Referer", "https://music.youtube.com/")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
            .addHeader("Content-Type", "application/json")
            .addHeader("Host", "music.youtube.com")
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = DataObject.fromJson(response.body!!.string())
        response.close()

        val optContents = responseBody.optArray("contents")
        if (!optContents.isPresent)
            return listOf()

        val renderer = optContents.get().getObject(0).getObject("searchSuggestionsSectionRenderer")
        val contents = renderer.optArray("contents").orElseGet(DataArray::empty)
        val results = mutableListOf<String>()
        contents.stream(DataArray::getObject).forEach {
            val result = it.getObject("searchSuggestionRenderer").getObject("navigationEndpoint").getObject("searchEndpoint").getString("query")
            if (result.length <= 100)
                results.add(result)
        }
        return results
    }
}

