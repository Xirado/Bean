package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.dsl.option
import at.xirado.bean.model.GuildFlag
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.koin.core.annotation.Single

private const val DEFAULT_EMBED_COLOR = 0x326da8

@Single
class AvatarCommand : SlashCommand("avatar", "Gets the avatar of a user") {
    init {
        option<User?>("user", "The user to get the avatar from")
        option<Boolean?>("server_profile", "Whether to get the avatar from the members' server-profile")

        requiredGuildFlags += GuildFlag.DEV_GUILD
    }

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent, user: User = event.user, serverProfile: Boolean = false) {
        val asMember = event.getOption<Member>("user")
            ?: event.member?.takeIf { user == event.user }

        val avatarUrl = when {
            asMember != null && serverProfile -> asMember.effectiveAvatar
            else -> user.effectiveAvatar
        }.getUrl(2048)

        val embedColor = when {
            asMember != null && !asMember.isDetached -> asMember.roles.firstOrNull { it.color != null }?.color?.rgb ?: DEFAULT_EMBED_COLOR
            else -> DEFAULT_EMBED_COLOR
        }

        val embedTitle = if (serverProfile && asMember != null)
            "${asMember.effectiveName}'s server-avatar"
        else
            "${user.name}'s avatar"

        event.replyEmbeds(Embed {
            title = embedTitle
            color = embedColor
            image = avatarUrl
        }).await()
    }
}