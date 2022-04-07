package at.xirado.bean.interaction.command.slash.developer

import at.xirado.bean.Application
import at.xirado.bean.interaction.CommandFlag
import at.xirado.bean.interaction.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

class EvalCommand(private val application: Application) : SlashCommand("eval", "eval some code") {

    init {
        setEnabledGuilds(*application.config.devGuilds.toLongArray())
        addCommandFlags(CommandFlag.DEVELOPER_ONLY)
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val codeInput = TextInput.create("eval", "Code", TextInputStyle.PARAGRAPH).build()

        event.replyModal(Modal.create("eval", "Groovy Eval").addActionRow(codeInput).build()).queue()
    }
}