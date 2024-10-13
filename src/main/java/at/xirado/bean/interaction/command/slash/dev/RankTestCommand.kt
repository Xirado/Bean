package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.interaction.autoDefer
import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.leveling.RankCardService
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.koin.core.annotation.Single
import org.koin.core.component.inject

@Single
class RankTestCommand : SlashCommand("rank-test", "rank-test") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        requiredUserFlags += UserFlag.ADMIN
    }

    private val rankCardService by inject<RankCardService>()

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent) {
        event.autoDefer {
            val rankCard = rankCardService.generateRankCard(event.member!!)
            MessageCreate(files = listOf(rankCard))
        }
    }
}