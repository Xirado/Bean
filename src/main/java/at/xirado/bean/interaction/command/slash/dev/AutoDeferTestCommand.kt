package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.interaction.autoDefer
import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.dsl.option
import at.xirado.bean.model.GuildFlag
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.koin.core.annotation.Single

@Single
class AutoDeferTestCommand : SlashCommand("auto-defer", "Tests auto-defer functionality") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        option<Int>("duration_millis", "How long to wait before replying")
        option<Boolean?>("ephemeral", "Whether the response should be ephemeral (Default: true)")
    }

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent, durationMillis: Int, ephemeral: Boolean = true) {
        event.autoDefer(ephemeral) {
            delay(durationMillis.toLong())

            MessageCreate {
                content = "Waited $durationMillis milliseconds"
            }
        }
    }
}