package at.xirado.bean.http.oauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Guild(
    val id: String,
    val name: String,
    val icon: String? = null,
    val owner: Boolean,
    val permissions: String,
    val features: List<String>,
    @SerialName("approximate_member_count")
    val approximateMemberCount: Int? = null,
    @SerialName("approximate_presence_count")
    val approximatePresenceCount: Int? = null,
    val botMetadata: BotMetadata? = null,
) {
    val iconUrl = icon?.let {
        if (it.startsWith("a_")) "https://cdn.discordapp.com/icons/$id/$it.gif"
        else "https://cdn.discordapp.com/icons/$id/$it.png"
    }
}

@Serializable
data class BotMetadata(
    val joined: Boolean,
)