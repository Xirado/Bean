package at.xirado.bean.audio

import at.xirado.bean.Application
import at.xirado.bean.interaction.components.SearchSuggestion
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import at.xirado.simplejson.json
import dev.minn.jda.ktx.util.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.hc.core5.net.URIBuilder
import java.net.URL

fun getBody(query: String, country: String, locale: String): String {
    return json {
        "input" by query
        "context" by {
            "client" by {
                "gl" by country
                "hl" by locale
                "deviceMake" by ""
                "deviceModel" by ""
                "userAgent" by "Mozilla/5.0"
                "clientName" by "WEB_REMIX"
                "clientVersion" by "1.20220330.01.00"
                "osName" by "Windows"
                "osVersion" by "10.0"
                "originalUrl" by "https://music.youtube.com/"
            }
        }
    }.toString().also { println(it) }
}

suspend fun getYoutubeMusicSearchResults(application: Application, query: String, country: String, locale: String) : List<SearchSuggestion> {
    val httpClient = application.httpClient

    val uri = URIBuilder().apply {
        scheme = "https"
        host = "music.youtube.com"
        path = "/youtubei/v1/music/get_search_suggestions"
    }

    val requestBody = getBody(query, country, locale).toRequestBody("application/json".toMediaType())



    val request = Request.Builder()
        .url(URL(uri.toString()))
        .post(requestBody)
        .addHeader("Referer", "https://music.youtube.com/")
        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
        .addHeader("Content-Type", "application/json")
        .addHeader("Host", "music.youtube.com")
        .build()

    val response = httpClient.newCall(request).await()
    val responseBody = JSONObject.fromJson(response.body!!.string())
    response.close()
    println(responseBody)

    val optContents = responseBody.optArray("contents")
    if (!optContents.isPresent)
        return listOf()

    val renderer = optContents.get().getObject(0).getObject("searchSuggestionsSectionRenderer")
    val contents = renderer.optArray("contents").orElseGet(JSONArray::empty)
    val results = mutableListOf<SearchSuggestion>()
    contents.stream(JSONArray::getObject).forEach {
        val result = it.getObject("searchSuggestionRenderer").getObject("navigationEndpoint").getObject("searchEndpoint").getString("query")
        if (result.length <= 100)
            results.add(SearchSuggestion(result))
    }
    return results
}