package at.xirado.bean.coroutines

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val virtualExecutor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
val virtualDispatcher = virtualExecutor.asCoroutineDispatcher()

val Dispatchers.Virtual: CoroutineDispatcher
    get() = virtualDispatcher

val virtualScheduledExecutor = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory())

inline fun <reified T> newCoroutineScope(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    job: Job = SupervisorJob(),
    context: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope {
    val logger = KotlinLogging.logger(T::class.java.name)
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(throwable) { "Uncaught exception from coroutine" }
        if (throwable is Error) {
            job.cancel()
            throw throwable
        }
    }

    return CoroutineScope(dispatcher + job + errorHandler + context)
}