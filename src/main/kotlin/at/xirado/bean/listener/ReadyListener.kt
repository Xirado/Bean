package at.xirado.bean.listener

import at.xirado.bean.Application
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ReadyListener(private val application: Application): ListenerAdapter() {

    private var ready = false

    override fun onGuildReady(event: GuildReadyEvent) {
        if (!ready) {
            application.interactionCommandHandler.init()
            ready = true
        }

        application.interactionCommandHandler.updateGuildCommands(event.guild)
    }
}