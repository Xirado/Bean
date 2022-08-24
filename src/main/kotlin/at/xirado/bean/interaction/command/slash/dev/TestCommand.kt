package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.i18n.LocalizedMessageReference
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.send
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent


class TestCommand(override val application: Application) : SlashCommand("test", "Some description") {
    init {
        devCommand()
        baseCommand = ::execute

        option<String>(name = "response_type", description = "The response type", required = true) {
            ResponseType.values().forEach { addChoice(it.toString(), it.toString()) }
        }
        option<Boolean>(name = "ephemeral", description = "Whether the response should be ephemeral", required = true)

        option<String>(name = "key", description = "Localization key")
    }

    suspend fun execute(event: SlashCommandInteractionEvent, responseType: String,
                        key: String = "general.unknown_error_occurred", ephemeral: Boolean) {
        val responseType = ResponseType.valueOf(responseType)

        val reference = LocalizedMessageReference.of(key)

        event.send(responseType, reference, ephemeral = ephemeral)
    }
}