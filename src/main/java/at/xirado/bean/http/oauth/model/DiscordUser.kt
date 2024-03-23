package at.xirado.bean.http.oauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordUser(
    val id: String,
    val username: String,
    val discriminator: String,
    @SerialName("global_name")
    val globalName: String?,
    val avatar: String?,
    val bot: Boolean = false,
    val system: Boolean = false,
    @SerialName("mfa_enabled")
    val mfaEnabled: Boolean = false,
    val banner: String? = null,
    @SerialName("accent_color")
    val accentColor: Int? = null,
    val locale: String? = null,
)
