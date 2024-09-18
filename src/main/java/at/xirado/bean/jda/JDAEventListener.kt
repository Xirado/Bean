package at.xirado.bean.jda

import at.xirado.bean.util.toEnumSet
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.koin.core.component.KoinComponent
import java.util.*

interface JDAEventListener : CoroutineEventListener, KoinComponent {
    val intents: EnumSet<GatewayIntent>
        get() = EnumSet.noneOf(GatewayIntent::class.java)
    val cacheFlags: EnumSet<CacheFlag>
        get() = EnumSet.noneOf(CacheFlag::class.java)
}

fun Collection<JDAEventListener>.allIntents() = flatMap { it.intents }.toEnumSet()

fun Collection<JDAEventListener>.allCacheFlags() = flatMap { it.cacheFlags }.toEnumSet()

fun Collection<JDAEventListener>.disabledCacheFlags() = CacheFlag.entries.minus(allCacheFlags()).toEnumSet()