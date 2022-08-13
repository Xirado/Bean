package at.xirado.bean.interaction.command.slash.moderation

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.sendSuccessMessage
import at.xirado.bean.util.sendWarningMessage
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.OffsetDateTime

private val NO_MESSAGE_ERROR_NOTICE = """
    I did not find any messages to delete :(
    Due to a Discord limitation, i can only delete messages that are up to 2 weeks old.
""".trimIndent()

class ClearCommand(override val application: Application) : SlashCommand("clear-messages", "Clears recent messages that are not older than 2 weeks") {

    init {
        addBotPermissions(Permission.MESSAGE_MANAGE)
        addUserPermissions(Permission.MESSAGE_MANAGE)

        option<Int>(name = "amount", description = "How many messages to delete", required = true) {
            setRequiredRange(1, 100)
        }
        option<User>(name = "user", description = "Optionally, delete only messages by this user")
    }

    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
        val channel = event.guildChannel
        val amount = event.getOption<Int>("amount")!!
        val user = event.getOption<User>("user")
        event.deferReply(true).queue()
        val messages = channel.iterableHistory.takeUntilAsync(if (user != null) 500 else 100) {
            it.timeCreated.isBefore(OffsetDateTime.now().minusWeeks(2))
        }.await()
            .apply { if (user != null) filter { it.author == user } }
            .take(amount)

        if (messages.isEmpty())
            return event.sendWarningMessage(NO_MESSAGE_ERROR_NOTICE).queue()

        if (messages.size == 1)
            messages[0].delete().await()
        else
            channel.deleteMessages(messages).await()

        event.sendSuccessMessage("Successfully deleted ${messages.size} messages!").queue()
    }
}