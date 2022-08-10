package at.xirado.bean.io.config

import at.xirado.bean.util.getLog
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import java.util.stream.Collectors

private val log = getLog<BeanConfiguration>()

class BeanConfiguration(jsonObject: JSONObject) {
    val discordToken: String
    val devMode: Boolean
    val devGuilds: List<Long>
    val devUsers: List<Long>
    val dbConfig: JSONObject
    val spotifyConfig: JSONObject
    val ytConfig: JSONObject

    init {
        discordToken = jsonObject.getStringOrThrow("discord_token")
        devMode = jsonObject.getBoolean("dev_mode", false)
        devGuilds = jsonObject.optArray("dev_guilds")
            .orElseGet(JSONArray::empty)
            .stream(JSONArray::getLong)
            .collect(Collectors.toUnmodifiableList())

        devUsers = jsonObject.optArray("dev_users")
            .orElseGet(JSONArray::empty)
            .stream(JSONArray::getLong)
            .collect(Collectors.toUnmodifiableList())

        if (devMode && devGuilds.isEmpty())
            log.warn("Dev mode is enabled but no guilds are specified. You will not see any commands!")

        dbConfig = jsonObject.optObject("database").orElseGet(JSONObject::empty)
        spotifyConfig = jsonObject.optObject("spotify").orElseGet(JSONObject::empty)
        ytConfig = jsonObject.optObject("youtube").orElseGet(JSONObject::empty)
    }
}