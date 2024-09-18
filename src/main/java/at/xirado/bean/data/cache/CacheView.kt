package at.xirado.bean.data.cache

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.runBlocking
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

abstract class CacheView<K : Comparable<K>, V>(maximumSize: Long) {
    private val lock = ReentrantReadWriteLock()
    private val cache = Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .build<K, V>()

    protected abstract suspend fun load(key: K): V?

    protected fun put(key: K, value: V) = write {
        cache.put(key, value)
    }

    context(CacheManager)
    operator fun set(key: K, value: V) = put(key, value)

    operator fun contains(key: K): Boolean = read {
        cache.getIfPresent(key) != null
    }

    suspend fun getById(key: K): V? {
        read {
            val cached = cache.getIfPresent(key)
            if (cached != null) return cached
        }

        val loaded = load(key) ?: return null
        put(key, loaded)
        return loaded
    }

    fun getByIdBlocking(key: K): V? = runBlocking { getById(key) }

    private inline fun <T> read(action: () -> T): T = lock.readLock().withLock(action)
    private inline fun <T> write(action: () -> T): T = lock.writeLock().withLock(action)
}

interface CacheManager