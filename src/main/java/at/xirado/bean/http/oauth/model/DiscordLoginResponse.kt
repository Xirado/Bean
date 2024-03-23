package at.xirado.bean.http.oauth.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscordLoginResponse(val token: String, val user: DiscordUser)