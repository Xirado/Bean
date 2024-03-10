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