package at.xirado.bean.http.oauth.model

import kotlinx.serialization.Serializable

@Serializable
data class SelfUserResponse(
    val user: DiscordUser,
    val guilds: List<DiscordGuild>? = null,
)