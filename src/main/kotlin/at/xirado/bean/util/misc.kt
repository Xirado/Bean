package at.xirado.bean.util

import at.xirado.bean.APPLICATION
import at.xirado.bean.data.guild.GuildData
import at.xirado.bean.data.user.UserData
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import at.xirado.simplejson.get
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.requests.RestAction
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <K, V> MutableMap<K, V>.computeSuspendIfAbsent(key: K, block: suspend (K) -> V): V {
    val value = this[key]
    if (value != null)
        return value

    return block.invoke(key).also { put(key, it) }
}

suspend fun User.getData() = APPLICATION.userManager.getUserData(idLong)
suspend fun Guild.getData() = APPLICATION.guildManager.getGuildData(idLong)

val Guild.data: GuildData
    get() = runBlocking { getData() }

val User.data: UserData
    get() = runBlocking { getData() }

fun GenericCommandInteractionEvent.getUserI18n() = APPLICATION.localizationManager.getForLanguageTag(userLocale.locale)
fun GenericCommandInteractionEvent.getGuildI18n() = APPLICATION.localizationManager.getForLanguageTag(guildLocale.locale)

fun JSONObject.arrayOrEmpty(key: String): JSONArray = optArray(key).orElseGet(JSONArray::empty)

fun JSONObject.noneNull(vararg keys: String): Boolean = keys.none { isNull(it) }

inline fun <reified T> JSONObject.getNullable(key: String): T? {
    if (isNull(key))
        return null
    return get<T>(key)
}

fun String.snakeCase() = map { if (it.isUpperCase()) "_${it.lowercase()}" else it }.joinToString(separator = "")

fun String.isUrl() = try { URL(this); true; } catch (ex: Exception) { false }

fun RestAction<*>.queueSilently() = queue({ }, { })

suspend fun Call.await(recordStack: Boolean = true): Response {
    val callStack = if (recordStack) {
        IOException().apply {
            stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
        }
    } else {
        null
    }

    return suspendCancellableCoroutine { cont ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isCancelled) return
                callStack?.initCause(e)
                cont.resumeWithException(callStack ?: e)
            }

            override fun onResponse(call: Call, response: Response) {
                cont.resume(response)
            }
        })

        cont.invokeOnCancellation {
            try {
                cancel()
            } catch (_: Throwable) {

            }
        }
    }
}

fun CharSequence.indicesOf(input: String): List<Int> =
    Regex(Pattern.quote(input)) // build regex
        .findAll(this)          // get the matches
        .map { it.range.first } // get the index
        .toCollection(mutableListOf()) // collect the result as list

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

    val response = APPLICATION.httpClient.newCall(request).await()

    val json = JSONObject.fromJson(response.body!!.byteStream())

    return if (raw)
        "https://hastebin.de/raw/${json.getString("key")}"
    else
        "https://hastebin.de/${json.getString("key")}${if (extension == null) "" else ".$extension"}"
}