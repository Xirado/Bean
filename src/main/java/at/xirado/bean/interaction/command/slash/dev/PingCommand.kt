package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.dsl.option
import at.xirado.bean.model.GuildFlag
import at.xirado.jdui.component.MessageComponentCallbackResult
import at.xirado.jdui.component.message.button.Button
import at.xirado.jdui.component.message.messageComponents
import at.xirado.jdui.message.messageBody
import at.xirado.jdui.persistence.PersistentMessageConfig
import at.xirado.jdui.persistence.PersistentMessageView
import at.xirado.jdui.replyPersistentView
import at.xirado.jdui.replyView
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import org.koin.core.annotation.Single

@Single
class PingCommand : SlashCommand("ping", "Will you get a pong?") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        option<Boolean?>("persists", "Whether the returned view persists")
    }

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent, persists: Boolean = false) {
        if (persists)
            event.replyPersistentView(AssertivePingPongView::class)
        else
            event.replyView(AssertivePingPongView()).await()
    }
}

class AssertivePingPongView : PersistentMessageView<AssertivePingPongState>(AssertivePingPongConfig) {
    private var counter: Int by state::counter
    private var lastUpdate: Instant by state::lastUpdate

    private val incrementButton = Button("inc", ButtonStyle.PRIMARY, label = "MORE?!") { _, _ ->
        counter++
        lastUpdate = Clock.System.now()
        MessageComponentCallbackResult.Noop
    }

    override suspend fun createMessage() = messageBody {
        embeds += Embed {
            description = buildString {
                append("Pong${ "!".repeat(counter) }\n\n")
                append(getLastUpdateTime())
            }
            color = 0xFF0000
        }
    }

    private fun getLastUpdateTime() = "Last update: ${getLastUpdate()}"

    private fun getLastUpdate() = TimeFormat.RELATIVE.format(lastUpdate.toEpochMilliseconds())

    override suspend fun defineComponents() = messageComponents {
        row { +incrementButton }
    }

    override fun invalidate(): Boolean {
        incrementButton.disabled = true
        return true
    }
}

@Serializable
data class AssertivePingPongState(
    var counter: Int = 0,
    var lastUpdate: Instant = Clock.System.now()
)

private object AssertivePingPongConfig : PersistentMessageConfig<AssertivePingPongState> {
    override val serializer = AssertivePingPongState.serializer()
}