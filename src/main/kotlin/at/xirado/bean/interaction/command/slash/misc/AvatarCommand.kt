package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType.USER

class AvatarCommand(private val application: Application) : SlashCommand("avatar", "gets a user's avatar") {

    init {
        option(type = USER, name = "user", description = "the user whose avatar you want to get")
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption<User>("user")?: event.user
        val embed = Embed {
            image = "${user.effectiveAvatarUrl}?size=1024"
            color = Application.BEAN_COLOR_ACCENT
            author {
                name = "${user.asTag}'s avatar"
                iconUrl = user.effectiveAvatarUrl
            }
        }

        event.replyEmbeds(embed).await()
    }
}