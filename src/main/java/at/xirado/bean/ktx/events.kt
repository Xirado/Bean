package at.xirado.bean.ktx

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.SubscribeEvent
import kotlin.coroutines.resume
import kotlin.time.Duration

suspend inline fun <reified T : GenericEvent> JDA.await(
    timeout: Duration,
    crossinline filter: (T) -> Boolean = { true }
): T? {
    return withTimeoutOrNull(timeout) {
        return@withTimeoutOrNull suspendCancellableCoroutine<T> {
            val listener = object : EventListener {
                @SubscribeEvent
                override fun onEvent(event: GenericEvent) {
                    if (event is T && filter(event)) {
                        removeEventListener(this)
                        it.resume(event)
                    }
                }
            }
            addEventListener(listener)
            it.invokeOnCancellation { removeEventListener(listener) }
        }
    }
}
