package at.xirado.bean.jdui

import at.xirado.bean.coroutines.virtualDispatcher
import at.xirado.bean.data.cache.PersistentMessageCacheView
import at.xirado.jdui.JDUIListener
import at.xirado.jdui.config.JDUIConfig
import at.xirado.jdui.config.PersistenceConfig
import net.dv8tion.jda.api.hooks.EventListener
import org.koin.core.annotation.Single

@Single
fun provideJDUI(cacheView: PersistentMessageCacheView): EventListener {
    val config = JDUIConfig {
        persistenceConfig = createPersistenceConfig(cacheView)
        coroutineDispatcher = virtualDispatcher
    }

    return JDUIListener(config)
}

private fun createPersistenceConfig(cacheView: PersistentMessageCacheView) = object : PersistenceConfig {
    override suspend fun retrieveState(id: Long): Pair<ByteArray, String>? {
        return cacheView.getById(id)?.let { it.data to it.className }
    }

    override suspend fun save(id: Long, data: ByteArray, clazz: String) {
        cacheView.save(id, data, clazz)
    }
}