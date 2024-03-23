package at.xirado.bean.http.oauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordGuild(
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
)