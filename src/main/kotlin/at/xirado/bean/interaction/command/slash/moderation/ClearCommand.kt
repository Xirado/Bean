package at.xirado.bean.interaction.command.slash.moderation

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.util.ResponseType
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.OffsetDateTime

class ClearCommand(override val app: Application) : SlashCommand("clear-messages") {
    override var description = "Clears recent messages that are not older than 2 weeks"

    private val noneFound = messageReference("commands.clear_messages.none_found")
    private val success = messageReference("commands.clear_messages.success")

    init {
        options {
            option<Int>(name = "amount", description = "How many messages to delete", required = true) { setRequiredRange(1, 100) }
            option<User>(name = "user", description = "Optionally, delete only messages by this user")
        }

        addBotPermissions(Permission.MESSAGE_MANAGE)
        addUserPermissions(Permission.MESSAGE_MANAGE)
    }

    @BaseCommand
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