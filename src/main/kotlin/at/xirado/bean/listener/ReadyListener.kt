package at.xirado.bean.listener

import at.xirado.bean.Application
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import java.util.concurrent.atomic.AtomicBoolean

class ReadyListener(private val application: Application) : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildReadyEvent -> onGuildReady(event, application)
        }
    }

    private var ready = AtomicBoolean(false)

    private fun onGuildReady(event: GuildReadyEvent, application: Application) {
        if (ready.compareAndSet(false, true)) {
            onReady(event, application)
        }
        application.interactionCommandHandler.updateGuildCommands(event.guild)
    }

    /**
     * This only runs once on every startup
     */
    private fun onReady(event: GuildReadyEvent, application: Application) {
        application.interactionCommandHandler.init()
    }
}