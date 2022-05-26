package at.xirado.bean.listener

import at.xirado.bean.Application
import dev.minn.jda.ktx.CoroutineEventListener
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class InteractionListener(private val application: Application) : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is SlashCommandInteractionEvent -> onSlashCommand(event, application)
            is CommandAutoCompleteInteractionEvent -> onAutoComplete(event, application)
        }
    }

    private suspend fun onSlashCommand(event: SlashCommandInteractionEvent, application: Application) {
        if (!event.isFromGuild) {
            event.reply("You can only execute this command from a guild!").await()
            return
        }
        application.interactionCommandHandler.handleCommand(event)
    }

    private suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent, application: Application) {
        if (!event.isFromGuild)
            return

        application.interactionCommandHandler.handleAutocomplete(event)
    }
}