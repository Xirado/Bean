package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ExceptionCommand(override val app: Application) : SlashCommand("exception") {
    init {
        options {
            option<Boolean>(name = "defer", description = "Whether to use deferReply()", required = true)
        }
        devCommand()
    }

    @BaseCommand
    fun execute(event: SlashCommandInteractionEvent, defer: Boolean) {
        if (defer)
            event.deferReply().queue()

        throw IllegalStateException("Some exception")
    }
}