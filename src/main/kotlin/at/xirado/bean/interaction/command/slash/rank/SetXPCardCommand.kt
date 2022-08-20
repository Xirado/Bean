package at.xirado.bean.interaction.command.slash.rank

import java.io.File

private val supportedExtensions = listOf("png", "jpg", "jpeg")
private val supportedExtensionsString = "`${supportedExtensions.joinToString(", ")}`"
val imageDirectory by lazy { File("backgrounds").also { if (!it.exists()) it.mkdir() } }
private val maxSize = 1000 * 1000 * 15 // 15MB

//class SetXPCardCommand(override val application: Application) : SlashCommand("setxpcard", "Update the background shown when using /rank") {
//
//    init {
//        option<Attachment>("background", "The image to upload. (1200x300 is ideal, 15MB max)", required = true)
//        option<Int>("color", "Primary accent color of the rank-card", required = true) {
//            choice("Red", 0xD0312D)
//            choice("Green", 0x32CD32)
//            choice("Blue", 0x0C71E0)
//            choice("Purple", 0x842BD7)
//            choice("Pink", 0xf542ec)
//            choice("Mint", 0x42f58d)
//            choice("Orange", 0xd48e15)
//        }
//    }
//
//    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
//        val attachment = event.getOption<Attachment>("background")!!
//        val extension = attachment.fileExtension
//        if (extension == null || extension !in supportedExtensions)
//            return event.replyErrorLocalized("commands.setxpcard.invalid_upload",
//                "extensions" to supportedExtensionsString, ephemeral = true).queue()
//
//        if (attachment.size > maxSize)
//            return event.replyErrorLocalized("commands.setxpcard.too_large",
//                "size" to "15MB", ephemeral = true).queue()
//
//        event.deferReply(true).queue()
//
//        val proxy = attachment.proxy
//
//        val fullName = "${event.interaction.idLong}.$extension"
//
//        val file = File(imageDirectory, fullName)
//        proxy.downloadToFile(file, 1200, 300).await()
//
//        val userData = event.user.getData()
//
//        userData.rankCardConfig.deleteBackground()
//
//        userData.update {
//            rankCardConfig.apply {
//                background = fullName
//                accentColor = event.getOption<Int>("color")!!
//            }
//        }
//
//        event.sendSuccessLocalized("commands.setxpcard.success").queue()
//    }
//}