package at.xirado.bean.interaction.command.slash.moderation

import at.xirado.bean.Application
import at.xirado.bean.i18n.LocalizedMessageReference
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.send
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.OffsetDateTime

class ClearCommand(override val application: Application) : SlashCommand("clear-messages", "Clears recent messages that are not older than 2 weeks") {

    private val noneFound = LocalizedMessageReference.of("commands.clear_messages.none_found")
    private val success = LocalizedMessageReference.of("commands.clear_messages.success")

    init {
        option<Int>(name = "amount", description = "How many messages to delete", required = true) { setRequiredRange(1, 100) }
        option<User>(name = "user", description = "Optionally, delete only messages by this user")

        addBotPermissions(Permission.MESSAGE_MANAGE)
        addUserPermissions(Permission.MESSAGE_MANAGE)

        baseCommand = ::execute
    }

    suspend fun execute(event: SlashCommandInteractionEvent, amount: Int, user: User?) {
        val channel = event.guildChannel
        event.deferReply(true).queue()
        val messages = channel.iterableHistory.takeWhileAsync(if (user != null) 500 else 100) { msg ->
            msg.timeCreated.isAfter(OffsetDateTime.now().minusWeeks(2))
        }.await()
            .filter { msg -> user?.let { msg.author == it } ?: true }
            .take(amount)

        if (messages.isEmpty())
            return event.send(ResponseType.WARNING, noneFound, "amount" to 0, ephemeral = true)

        if (messages.size == 1)
            messages[0].delete().await()
        else
            channel.deleteMessages(messages).await()

        event.send(ResponseType.SUCCESS, success, "amount" to messages.size, ephemeral = true)
    }
}