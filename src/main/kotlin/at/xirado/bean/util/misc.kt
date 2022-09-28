package at.xirado.bean.util

import at.xirado.bean.Application
import at.xirado.bean.command.DiscordCommand
import at.xirado.bean.data.guild.GuildData
import at.xirado.bean.data.user.UserData
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import at.xirado.simplejson.get
import dev.minn.jda.ktx.util.await
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import net.dv8tion.jda.api.requests.RestAction
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.util.regex.Pattern

suspend fun <K, V> MutableMap<K, V>.computeSuspendIfAbsent(key: K, block: suspend (K) -> V): V {
    val value = this[key]
    if (value != null)
        return value

    return block.invoke(key).also { put(key, it) }
}

context(Application)
suspend fun User.retrieveData(): UserData = userManager.getUserData(idLong)

context(Application)
suspend fun Guild.retrieveData(): GuildData = guildManager.getGuildData(idLong)

context(Application)
fun GenericInteractionCreateEvent.getUserI18n() = localizationManager.getForLanguageTag(userLocale.locale)

context(Application)
fun GenericInteractionCreateEvent.getGuildI18n() = localizationManager.getForLanguageTag(guildLocale.locale)

fun JSONObject.arrayOrEmpty(key: String): JSONArray = optArray(key).orElseGet(JSONArray::empty)

inline fun <reified T> JSONObject.getNullable(key: String) = if (isNull(key)) null else get<T>(key)

fun JSONObject.noneNull(vararg keys: String): Boolean = keys.none { isNull(it) }

val ModalMapping.asNullableString: String?
    get() = asString.let { it.ifEmpty { null } }

fun Button.disableIf(block: (Button) -> Boolean): Button {
    return if (block.invoke(this))
        asDisabled()
    else
        this
}

fun ActionRow.disableIf(block: (ActionRow) -> Boolean): ActionRow {
    return if (block.invoke(this))
        asDisabled()
    else
        this
}

fun String.snakeCase() = map { if (it.isUpperCase()) "_${it.lowercase()}" else it }.joinToString(separator = "")

fun String.isUrl() = try { URL(this); true; } catch (ex: Exception) { false }

fun RestAction<*>.queueSilently() = queue({ }, { })

fun CharSequence.indicesOf(input: String): List<Int> =
    Regex(Pattern.quote(input)) // build regex
        .findAll(this)          // get the matches
        .map { it.range.first } // get the index
        .toCollection(mutableListOf()) // collect the result as list

context(DiscordCommand)
suspend fun postHaste(text: String, raw: Boolean = false, extension: String? = null): String {
    val data = text.toByteArray()
    val length = data.size

    val body = data.toRequestBody(contentType = "text/plain".toMediaType())

    val request = Request.Builder().apply {
        url("https://hastebin.de/documents")
        post(body)
        header("User-Agent", "Bean Discord Bot (https://github.com/Xirado/Bean)")
        header("Content-Length", length.toString())
    }.build()

    val response = app.httpClient.newCall(request).await()

    val json = JSONObject.fromJson(response.body!!.byteStream())

    return if (raw)
        "https://hastebin.de/raw/${json.getString("key")}"
    else
        "https://hastebin.de/${json.getString("key")}${if (extension == null) "" else ".$extension"}"
}

fun JSONObject.getKeys(prevPath: String? = null): List<String> {
    val keys = mutableListOf<String>()
    keys().forEach { key ->
        val result = get(key)
        if (result !is Map<*, *>) {
            keys += if (prevPath == null) key else "$prevPath.$key"
            return@forEach
        }
        keys.addAll(getObject(key).getKeys(if (prevPath == null) key else "$prevPath.$key"))
    }
    return keys
}