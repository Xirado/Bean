package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.util.ColorPalette
import at.xirado.bean.util.getGuildI18n
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AvatarCmd(override val app: Application) : SlashCommand("avatar", "Gets the avatar of a user.") {
    init {
        options {
            option<User>(name = "user", description = "User to get the avatar from.")
        }
    }

    @BaseCommand
    fun execute(event: SlashCommandInteractionEvent, user: User = event.user) {
        val locale = with (app) { event.getGuildI18n() }
        val embed = Embed {
            color = ColorPalette.PRIMARY.rgb
            image = "${user.effectiveAvatarUrl}?size=1024"
            author(name = locale.getOrDefault("commands.avatar.title", "user" to user.asTag))
        }
        event.replyEmbeds(embed).queue()
    }
}