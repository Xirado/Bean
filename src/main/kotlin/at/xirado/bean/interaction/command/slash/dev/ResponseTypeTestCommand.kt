package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.i18n.LocalizedMessageReference
import at.xirado.bean.interaction.slash.AutoComplete
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.getKeys
import at.xirado.bean.util.getUserI18n
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ResponseTypeTestCommand(override val app: Application) : SlashCommand("response_type_test") {
    init {
        options {
            option<String>(name = "response_type", description = "The response type", required = true) {
                ResponseType.values().forEach { addChoice(it.toString(), it.toString()) }
            }
            option<Boolean>(name = "ephemeral", description = "Whether the response should be ephemeral", required = true)
            option<String>(name = "key", description = "Localization key", autocomplete = true)
        }
        devCommand()
    }

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent, responseType: String,
                        key: String = "general.unknown_error_occurred", ephemeral: Boolean) {
        val responseType = ResponseType.valueOf(responseType)
        val reference = LocalizedMessageReference(key)
        event.send(responseType, reference, ephemeral = ephemeral)
    }

    @AutoComplete("key")
    fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
        val locale = with(app) { event.getUserI18n() }
        val keys = locale.data.getKeys()
        event.replyChoiceStrings(
            keys.filter { it.startsWith(event.focusedOption.value) }
                .take(25)
        ).queue()
    }
}