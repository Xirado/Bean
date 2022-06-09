package at.xirado.bean.util

import at.xirado.bean.APPLICATION
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

fun JSONObject.arrayOrEmpty(key: String): JSONArray = optArray(key).orElseGet(JSONArray::empty)

fun DataObject.noneNull(vararg keys: String): Boolean = keys.none { isNull(it) }

fun isUrl(url: String): Boolean = try { URL(url); true; } catch (ex: Exception) { false }

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