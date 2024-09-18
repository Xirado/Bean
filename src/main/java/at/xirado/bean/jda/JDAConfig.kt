package at.xirado.bean.jda

import kotlinx.serialization.Serializable

@Serializable
data class JDAConfig(
    val token: String,
    val gatewayProxy: ProxiedGatewayConfig? = null,
)

@Serializable
data class ProxiedGatewayConfig(
    val enabled: Boolean,
    val url: String,
    val shardsTotal: Int,
)