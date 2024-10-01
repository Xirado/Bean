package at.xirado.bean.jda

import at.xirado.bean.coroutines.virtualDispatcher
import at.xirado.jdui.utils.newCoroutineScope
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.CoroutineEventManager
import kotlinx.coroutines.CoroutineScope
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import java.util.*

@Single(createdAtStart = true)
class JDAEventManager(
    scope: CoroutineScope = newCoroutineScope<JDAEventManager>(virtualDispatcher),
) : CoroutineEventManager(scope), KoinComponent {
    init {
        registerListeners()
    }

    private fun registerListeners() {
        val listeners = getKoin().let {
            it.getAll<JDAEventListener>() + it.getAll<EventListener>() + it.getAll<CoroutineEventListener>()
        }
        listeners.forEach {
            register(it)
        }
    }

    val requiredIntents: EnumSet<GatewayIntent>
        get() = listeners.filterJDAEventListeners().allIntents()
    val enabledCacheFlags: EnumSet<CacheFlag>
        get() = listeners.filterJDAEventListeners().allCacheFlags()
    val disabledCacheFlags: EnumSet<CacheFlag>
        get() = listeners.filterJDAEventListeners().disabledCacheFlags()

    private fun Collection<Any>.filterJDAEventListeners()
      = mapNotNull { it as? JDAEventListener }
}