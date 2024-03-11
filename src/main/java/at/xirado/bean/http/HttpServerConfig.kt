package at.xirado.bean.http

import kotlinx.serialization.Serializable

@Serializable
data class HttpServerConfig(
    val host: String,
    val port: Int,
    val jwt: JWTConfig,
    val corsAllowAll: Boolean = false,
)

@Serializable
data class JWTConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
)

@Serializable
data class OAuthConfig(
    val clientId: Long,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: List<String>
)