package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.sendErrorMessage
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType.USER

class BannerCommand(private val application: Application) : SlashCommand("banner", "gets a user's banner") {

    init {
        option(type = USER, name = "user", description = "the user whose banner you want to get")
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        val user = event.getOption<User>("user")?: event.user

        val profile = user.retrieveProfile().await()

        if (profile.bannerUrl == null) {
            event.sendErrorMessage("${user.asTag} has no banner!").await()
            return
        }

        val embed = Embed {
            image = "${profile.bannerUrl}?size=4096"
            color = Application.BEAN_COLOR_ACCENT
            author {
                name = "${user.asTag}'s banner"
                iconUrl = user.effectiveAvatarUrl
            }
        }

        event.hook.sendMessageEmbeds(embed).queue()
    }
}