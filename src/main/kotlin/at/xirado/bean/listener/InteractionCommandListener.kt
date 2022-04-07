package at.xirado.bean.listener

import at.xirado.bean.Application
import dev.minn.jda.ktx.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class InteractionCommandListener(private val application: Application) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        application.coroutineScope.launch {
            if (!event.isFromGuild) {
                event.reply("You can only execute this command from a guild!").await()
                return@launch
            }
            application.interactionCommandHandler.handleCommand(event)
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        application.coroutineScope.launch {
            if (!event.isFromGuild)
                return@launch

            application.interactionCommandHandler.handleAutocomplete(event)
        }
    }
}