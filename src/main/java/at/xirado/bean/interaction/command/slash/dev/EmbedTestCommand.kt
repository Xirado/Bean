package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.embed.EmbedService
import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.dsl.option
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import at.xirado.bean.model.toMessageEmbed
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.koin.core.annotation.Single
import org.koin.core.component.inject

@Single
class EmbedTestCommand : SlashCommand("embed-test", "Tests embed preset functionality") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        requiredUserFlags += UserFlag.ADMIN

        option<String>("embed_name", "Name of the embed")
    }

    private val embedService by inject<EmbedService>()

    @Handler(ephemeral = true)
    suspend fun run(event: SlashCommandInteractionEvent, embedName: String): MessageCreateData {
        val embed = embedService.getEmbed(embedName)

        val messageEmbed = embed.toMessageEmbed()

        return MessageCreate(embeds = listOf(messageEmbed))
    }
}