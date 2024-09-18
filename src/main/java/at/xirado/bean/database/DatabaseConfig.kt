package at.xirado.bean.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String
)