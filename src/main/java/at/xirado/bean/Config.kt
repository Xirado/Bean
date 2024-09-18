package at.xirado.bean

import at.xirado.bean.data.enumSetOf
import at.xirado.bean.database.DatabaseConfig
import at.xirado.bean.jda.JDAConfig
import at.xirado.bean.model.GuildFeature
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val database: DatabaseConfig,
    val jda: JDAConfig,
    val defaultGuildFeatures: List<String> = emptyList(),
) {
    val defaultGuildFeaturesParsed by lazy {
        val features = enumSetOf<GuildFeature>()

        defaultGuildFeatures.map { name ->
            val feature = GuildFeature.entries.find { it.name == name }
                ?: throw IllegalStateException("No GuildFeature found with name $name")

            features += feature
        }

        features
    }
}