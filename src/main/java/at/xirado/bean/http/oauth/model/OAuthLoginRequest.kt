package at.xirado.bean.http.oauth.model

import kotlinx.serialization.Serializable

@Serializable
data class OAuthLoginRequest(
    val code: String,
)