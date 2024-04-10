package at.xirado.bean.ktx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

context(CoroutineScope)
fun <T : Any, R> T.letAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(T) -> R
): Deferred<R> {
    val scopeBlock: suspend CoroutineScope.() -> R = {
        block(this@letAsync)
    }

    return async(context, start, scopeBlock)
}