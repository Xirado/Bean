package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.AnsiBackgroundColor
import at.xirado.bean.util.AnsiForegroundColor
import at.xirado.bean.util.ansi
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AnsiTestCommand(override val application: Application) : SlashCommand("ansi_test", "Ansi magic baby") {
    init {
        devCommand()
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
    }

    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
        val text = event.getOption<String>("text")!!
        val foreground = AnsiForegroundColor.fromValue(event.getOption<Int>("foreground"))
        val background = AnsiBackgroundColor.fromValue(event.getOption<Int>("background"))
        val bold = event.getOption<Boolean>("bold")?: false
        val underlined = event.getOption<Boolean>("underline")?: false
        val sendAsFile = event.getOption<Boolean>("send_as_file")?: false

        val result = ansi(foreground, background, bold, underlined) { text }

        if (sendAsFile)
            event.replyFile(result.toByteArray(), "result.ansi").queue()
        else
            event.reply("```ansi\n$result```").queue()
    }
}