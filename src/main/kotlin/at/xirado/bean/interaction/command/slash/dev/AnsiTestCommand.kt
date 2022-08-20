package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.AnsiBackgroundColor
import at.xirado.bean.util.AnsiForegroundColor
import at.xirado.bean.util.ansi
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AnsiTestCommand(override val application: Application) : SlashCommand("ansi_test", "Send ANSI-Formatted messages") {
    init {
        option<String>(name = "text", description = "The text to apply ansi on", required = true)
        option<Int>(name = "foreground", description = "Foreground color") {
            AnsiForegroundColor.values().forEach { addChoice(it.name, it.value.toLong()) }
        }
        option<Int>(name = "background", description = "Background color") {
            AnsiBackgroundColor.values().forEach { addChoice(it.name, it.value.toLong()) }
        }
        option<Boolean>(name = "bold", description = "Whether the text should be bold")
        option<Boolean>(name = "underline", description = "Whether the text should be underlined")
        option<Boolean>(name = "send_as_file", description = "Whether the result should be sent in a file or not")

        devCommand()
        baseCommand = ::execute
    }

    fun execute(event: SlashCommandInteractionEvent, text: String, foreground: Int?, background: Int?,
                bold: Boolean = false, underline: Boolean = false, sendAsFile: Boolean = false ) {

        val foreground = AnsiForegroundColor.fromValue(foreground)
        val background = AnsiBackgroundColor.fromValue(background)

        val result = ansi(foreground, background, bold, underline) { text }

        if (sendAsFile)
            event.replyFile(result.toByteArray(), "result.ansi").queue()
        else
            event.reply("```ansi\n$result```").queue()
    }
}