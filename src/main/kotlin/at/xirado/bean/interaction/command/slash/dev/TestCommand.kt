package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.createResponseEmbed
import at.xirado.bean.util.getUserI18n
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TestCommand(override val application: Application) : SlashCommand("test", "Some description") {
    init {
        devCommand()
        baseCommand = ::execute

        option<String>(name = "response_type", description = "The response type", required = true) {
            ResponseType.values().forEach { addChoice(it.toString(), it.toString()) }
        }
        option<String>(name = "key", description = "Localization key")
    }

    suspend fun execute(event: SlashCommandInteractionEvent, responseType: String, key: String = "general.unknown_error_occurred") {
        val responseType = ResponseType.valueOf(responseType)
        val userI18n = event.getUserI18n()
        val message = userI18n.localizedMessage(key)

        event.replyEmbeds(createResponseEmbed(responseType, message)).await()
    }
}