package at.xirado.bean.listener

import at.xirado.bean.Application
import dev.minn.jda.ktx.CoroutineEventListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class LegacyCommandListener(val application: Application) : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        if (event is MessageReceivedEvent) {
            if (!event.isFromGuild || event.isWebhookMessage || event.author.isBot)
                return

            application.legacyCommandHandler.handleCommand(event)
        }
    }
}