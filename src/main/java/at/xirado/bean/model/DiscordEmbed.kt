package at.xirado.bean.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.utils.data.DataObject

@Serializable
data class Embed(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val timestamp: String? = null,
    val color: Int? = null,
    val footer: Footer? = null,
    val image: Image? = null,
    val thumbnail: Thumbnail? = null,
    val video: Video? = null,
    val provider: Provider? = null,
    val author: Author? = null,
    val fields: List<Field> = emptyList(),
)

fun Embed.toDataObject() = DataObject.fromJson(Json.encodeToString(this))

@Serializable
data class Footer(
    val text: String,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("proxy_icon_url")
    val proxyIconUrl: String? = null,
)

@Serializable
data class Field(
    val name: String,
    val value: String,
    val inline: Boolean? = null,
)

@Serializable
data class Author(
    val name: String,
    val url: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("proxy_icon_url")
    val proxyIconUrl: String? = null,
)

@Serializable
data class Image(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
data class Thumbnail(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
data class Video(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
data class Provider(
    val name: String? = null,
    val url: String? = null,
)