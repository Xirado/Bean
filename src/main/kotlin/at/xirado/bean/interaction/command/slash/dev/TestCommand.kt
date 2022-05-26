package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TestCommand(override val application: Application) : SlashCommand("test", "This is a test command") {

    init {
        devCommand()
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.reply(event.jda.guildCache.joinToString(", ") { "${it.name} ${it.idLong}" }).queue()
    }
}