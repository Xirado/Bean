package at.xirado.bean.interaction.command.slash.rank

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.retrieveData
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.choice
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.io.File

private val supportedExtensions = listOf("png", "jpg", "jpeg")
private val supportedExtensionsString = "`${supportedExtensions.joinToString(", ")}`"
val imageDirectory by lazy { File("backgrounds").also { if (!it.exists()) it.mkdir() } }
private val maxSize = 1024 * 1024 * 15 // 15MiB

class SetXPCardCommand(override val app: Application) : SlashCommand("setxpcard") {
    override var description = "Update the background shown when using /rank"

    init {
        options {
            option<Attachment>("background", "The image to upload. (1200x300 is ideal, 15MiB max)", required = true)
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
    }

    private val invalidUpload = messageReference("commands.setxpcard.invalid_upload")
    private val tooLarge = messageReference("commands.setxpcard.too_large")
    private val success = messageReference("commands.setxpcard.success")

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent, background: Attachment, color: Int) {
        val extension = background.fileExtension
        if (extension == null || extension !in supportedExtensions)
            return event.send(ResponseType.ERROR, invalidUpload, "extensions" to supportedExtensionsString, ephemeral = true)

        if (background.size > maxSize)
            return event.send(ResponseType.ERROR, tooLarge, "size" to "15MiB", ephemeral = true)

        event.deferReply(true).queue()

        val proxy = background.proxy

        val fullName = "${event.interaction.idLong}.$extension"

        val file = File(imageDirectory, fullName)
        proxy.downloadToFile(file, 1200, 300).await()

        val userData = with(app) { event.user.retrieveData() }

        userData.rankCardConfig.deleteBackground()

        userData.update {
            rankCardConfig.apply {
                this.background = fullName
                this.accentColor = color
            }
        }

        event.send(ResponseType.SUCCESS, success, ephemeral = true)
    }
}