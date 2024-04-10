package at.xirado.bean.misc

import at.xirado.bean.Bean
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val log = LoggerFactory.getLogger(Bean::class.java)

fun createCoroutineScope(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    job: Job = SupervisorJob(),
    errorHandler: CoroutineExceptionHandler? = null,
    context: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope {
    val handler = errorHandler ?: CoroutineExceptionHandler { _, throwable ->
        log.error("Uncaught exception from coroutine", throwable)
        if (throwable is Error) {
            job.cancel()
            throw throwable
        }
    }
    return CoroutineScope(dispatcher + job + handler + context)
}