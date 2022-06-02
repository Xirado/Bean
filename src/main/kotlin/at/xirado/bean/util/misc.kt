package at.xirado.bean.util

import at.xirado.bean.APPLICATION
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

suspend fun <K, V> MutableMap<K, V>.computeSuspendIfAbsent(key: K, block: suspend (K) -> V): V {
    val value = this[key]
    if (value != null)
        return value

    return block.invoke(key).also { put(key, it) }
}

suspend fun User.getData() = APPLICATION.userData.getUserData(idLong)
suspend fun Guild.getData() = APPLICATION.guildManager.getGuildData(idLong)

fun GenericCommandInteractionEvent.getUserI18n() = APPLICATION.localizationManager.getForLanguageTag(userLocale.toLanguageTag())
fun GenericCommandInteractionEvent.getGuildI18n() = APPLICATION.localizationManager.getForLanguageTag(guildLocale.toLanguageTag())

fun JSONObject.arrayOrEmpty(key: String) = optArray(key).orElseGet(JSONArray::empty)