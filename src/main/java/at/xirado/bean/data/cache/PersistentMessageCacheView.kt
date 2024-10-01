package at.xirado.bean.data.cache

import at.xirado.bean.data.DBContext
import at.xirado.bean.database.entity.PersistentMessageView
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
class PersistentMessageCacheView(
    private val db: DBContext
) : CacheView<Long, PersistentMessageView>(1000) {
    suspend fun save(id: Long, data: ByteArray, className: String): PersistentMessageView = db.doTransaction {
        val view = getById(id)?.also {
            it.data = data
            it.className = className
        } ?: PersistentMessageView.new(id) {
            this.data = data
            this.className = className
        }

        put(id, view)
        view
    }

    override suspend fun load(key: Long): PersistentMessageView? = db.doTransaction {
        PersistentMessageView.findById(key)
    }
}