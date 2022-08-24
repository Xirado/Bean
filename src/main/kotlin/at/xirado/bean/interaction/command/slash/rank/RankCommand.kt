package at.xirado.bean.interaction.command.slash.rank

import at.xirado.bean.Application
import at.xirado.bean.i18n.LocalizedMessageReference
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.manager.generateCard
import at.xirado.bean.manager.retrieveTotalExperience
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.send
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload

class RankCommand(override val application: Application) : SlashCommand("rank", "Displays the rank-card of a user.") {

    private val notRankedSelf = LocalizedMessageReference.of("commands.rank.not_ranked_self")
    private val notRanked = LocalizedMessageReference.of("commands.rank.not_ranked")

    init {
        option<User>(name = "user", description = "User to display the rank-card of.")

        baseCommand = ::execute
    }

    suspend fun execute(event: SlashCommandInteractionEvent, user: User = event.user) {
        val experience = retrieveTotalExperience(event.guild!!.idLong, user.idLong)
        if (experience < 100)
            return if (user == event.user)
                event.send(ResponseType.ERROR, notRankedSelf, ephemeral = true)
            else
                event.send(ResponseType.ERROR, notRanked, ephemeral = true)

        event.deferReply().queue()

        val rankCard = generateCard(user, event.guild!!)

        event.hook.sendFiles(FileUpload.fromData(rankCard, "card.png")).queue()
    }
}