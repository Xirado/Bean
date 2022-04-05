package at.xirado.bean.listener

import at.xirado.bean.Application
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class InteractionCommandListener(val application: Application) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        event.reply("Hello").queue()
    }
}