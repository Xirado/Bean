package at.xirado.bean.i18n

import at.xirado.bean.util.getLog
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val cache = ConcurrentHashMap<String, LocalizedMessageReference>()

private val log = getLog<LocalizedMessageReference>()

private val lock = ReentrantLock()

class LocalizedMessageReference private constructor(val path: String) {

    init {
        lock.withLock {
            if (default != null)
                default!!.get(path) ?: log.error("Default-Locale \"${default!!.fileName}\" does not contain path \"$path\"!", NoSuchFieldException())
        }

    }

    fun get(i18n: I18n, vararg attributes: Pair<String, Any>): String {
        return i18n.get(path, *attributes) ?: i18n.manager.default.getValue(path, *attributes)
    }

    companion object {
        var default: I18n? = null
            set(value) = lock.withLock { field = value }

        fun of(path: String): LocalizedMessageReference {
            if (cache.containsKey(path))
                return cache[path]!!
            return LocalizedMessageReference(path).also { cache[path] = it }
        }
    }
}