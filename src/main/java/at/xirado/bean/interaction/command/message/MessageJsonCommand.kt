package at.xirado.bean.interaction.command.message

import at.xirado.bean.interaction.command.model.message.MessageContextCommand
import at.xirado.bean.model.GuildFlag
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.data.DataPath
import org.koin.core.annotation.Single

@Single
class MessageJsonCommand : MessageContextCommand("JSON") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
    }

    override suspend fun execute(event: MessageContextInteractionEvent) {
        val targetId = event.target.idLong
        val messageJson = DataPath.getObject(event.rawData!!, "d.data.resolved.messages.$targetId")
        val prettyJson = messageJson.toPrettyString()

        if (prettyJson.length > Message.MAX_CONTENT_LENGTH - 11) {
            val fileUpload = FileUpload.fromData(prettyJson.toByteArray(), "message.json")
            event.replyFiles(fileUpload).setEphemeral(true).await()
        } else {
            event.reply("```json\n$prettyJson```").setEphemeral(true).await()
        }
    }
}