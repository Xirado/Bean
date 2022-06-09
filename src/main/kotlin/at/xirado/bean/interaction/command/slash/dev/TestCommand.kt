package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.replyWarningLocalized
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TestCommand(override val application: Application) : SlashCommand("test", "Tests some stuff") {
    init {
        devCommand(requiresAdmin = true)
    }

    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
        event.replyWarningLocalized("general.must_be_listening_in_bot_vc").queue()
    }
}