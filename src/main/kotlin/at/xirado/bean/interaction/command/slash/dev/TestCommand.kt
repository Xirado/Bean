package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.manager.generateCard
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TestCommand(override val application: Application) : SlashCommand("test", "This is a test command") {

    init {
        devCommand()
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        event.hook.sendFile(generateCard(event.user, event.guild!!), "rank.png").queue()

    }
}