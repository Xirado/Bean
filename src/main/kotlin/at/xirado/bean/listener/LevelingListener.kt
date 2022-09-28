package at.xirado.bean.listener

import at.xirado.bean.Application
import at.xirado.bean.coroutineScope
import at.xirado.bean.manager.addExperience
import at.xirado.bean.util.retrieveData
import dev.minn.jda.ktx.events.CoroutineEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

class LevelingListener(val application: Application) : CoroutineEventListener {
    private val coolDown = if (application.config.devMode) 0 else 60000

    init {
        coroutineScope.launch {
            while (true) {
                for (entry in timeoutCache) {
                    val user = entry.key
                    val timeout = entry.value
                    if (timeout + coolDown < System.currentTimeMillis())
                        timeoutCache.remove(user)
                }
                delay(5.minutes)
            }
        }
    }

    private val timeoutCache = ConcurrentHashMap<Long, Long>()
    private val random = Random()

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReceivedEvent -> onGuildMessageReceived(event)
        }
    }

    suspend fun onGuildMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild || event.isWebhookMessage || event.author.isBot)
            return

        val guild = event.guild
        val data = with(application) { guild.retrieveData() }
        val user = event.author
        val userId = user.idLong
        val guildId = guild.idLong

        if (event.channel.idLong in data.levelingConfig.blacklistedChannels)
            return

        if (isTimeout(userId))
            return

        val minExperience = data.levelingConfig.minExperience
        val maxExperience = data.levelingConfig.maxExperience

        val difference = maxExperience - minExperience

        val addedExperience = minExperience + random.nextInt(difference + 1)

        addExperience(guildId, userId, addedExperience, user.name, user.discriminator)

        timeoutCache[userId] = System.currentTimeMillis()
    }

    private fun isTimeout(userId: Long): Boolean {
        if (userId !in timeoutCache.keys)
            return false

        return timeoutCache[userId]!! + coolDown > System.currentTimeMillis()
    }
}