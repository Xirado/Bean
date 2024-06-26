package at.xirado.bean.http.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscordLoginUrlResponse(val url: String)

@Serializable
data class DiscordInviteUrlResponse(val url: String)
