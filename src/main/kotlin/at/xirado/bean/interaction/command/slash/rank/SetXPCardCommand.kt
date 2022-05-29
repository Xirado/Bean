package at.xirado.bean.interaction.command.slash.rank

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import dev.minn.jda.ktx.interactions.choice
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.io.File

private val supportedExtensions = listOf("png", "jpg", "jpeg")
private val imageDirectory by lazy { File("backgrounds").also { if (!it.exists()) it.mkdir() } }
private val maxSize = 1000 * 1000 * 15 // 15MB

class SetXPCardCommand(override val application: Application) : SlashCommand("setxpcard", "Update the background shown when using /rank") {

    init {
        option<Attachment>("background", "The image to upload. (1200x300 is ideal, 15MB max)", required = true)
        option<Int>("color", "Primary accent color of the rank-card", required = true) {
            choice("Red", 0xD0312D)
                choice("Green", 0x32CD32)
                choice("Blue", 0x0C71E0)
                choice("Purple", 0x842BD7)
                choice("Pink", 0xf542ec)
                choice("Mint", 0x42f58d)
                choice("Orange", 0xd48e15)
        }
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }
}