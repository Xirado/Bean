package at.xirado.bean.interaction.command.slash.rank

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.manager.generateCard
import at.xirado.bean.manager.retrieveTotalExperience
import at.xirado.bean.util.ResponseType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload

class RankCommand(override val app: Application) : SlashCommand("rank") {
    override var description = "Displays the rank-card of a user."

    private val notRankedSelf = messageReference("commands.rank.not_ranked_self")
    private val notRanked = messageReference("commands.rank.not_ranked")

    init {
        options {
            option<User>(name = "user", description = "User to display the rank-card of.")
        }
    }

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent, user: User = event.user) {
        val experience = retrieveTotalExperience(event.guild!!.idLong, user.idLong)
        if (experience < 100)
            return if (user == event.user)
                event.send(ResponseType.ERROR, notRankedSelf, ephemeral = true)
            else
                event.send(ResponseType.ERROR, notRanked, ephemeral = true)

        event.deferReply().queue()

        val rankCard = with(app) { generateCard(user, event.guild!!) }

        event.hook.sendFiles(FileUpload.fromData(rankCard, "card.png")).queue()
    }
}