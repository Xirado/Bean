package at.xirado.bean

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

@Serializable
data class Config(
    val discordToken: String,
    val debugMode: Boolean = false,
    val webhookUrl: String? = null,
    val http: HttpServerConfig,
    val jwt: JWTConfig,
    val db: DatabaseConfig,
    val oauth: OAuthConfig,
)

@Serializable
data class HttpServerConfig(
    val host: String,
    val port: Int,
    val corsAllowAll: Boolean = false,
)

@Serializable
data class DatabaseConfig(
    val host: String,
    val database: String,
    val username: String,
    val password: String,
    val port: Int,
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

fun readConfig(copyPresetIfMissing: Boolean = false): Config {
    val configPath = Path("config.toml")

    if (!configPath.exists()) {
        if (copyPresetIfMissing) {
            copyConfigPreset(configPath)
            throw IllegalStateException("config created. exiting.")
        } else {
            throw IllegalStateException("Could not locate config file \"config.toml\"")
        }
    }

    val configContent = configPath.readText()
    return Toml.decodeFromString(configContent)
}

private fun copyConfigPreset(to: Path) {
    val preset = Bean::class.java.getResourceAsStream("/config.toml")
        ?: throw IllegalStateException("Could not find config.toml preset in resources")

    preset.use { Files.copy(it, to) }
}

