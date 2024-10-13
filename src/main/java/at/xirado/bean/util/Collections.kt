package at.xirado.bean.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <reified T : Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> {
    val set = toSet()

    return if (set.isEmpty())
        EnumSet.noneOf(T::class.java)
    else
        EnumSet.copyOf(set)
}

context(CoroutineScope)
inline fun <T, R> Iterable<T>.mapAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.(T) -> R
) : List<Deferred<R>> = map { async(context, start) { block(this, it) } }

context(CoroutineScope)
inline fun <T, R> Iterable<T>.mapIndexedAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.(Int, T) -> R
) : List<Deferred<R>> = mapIndexed { int, t -> async(context, start) { block(this, int, t) } }