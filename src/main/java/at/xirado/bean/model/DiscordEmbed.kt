package at.xirado.bean.model

import at.xirado.bean.interpolator.Interpolatable
import at.xirado.bean.interpolator.InterpolationContext
import at.xirado.bean.interpolator.Interpolator
import dev.minn.jda.ktx.messages.Embed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.data.DataObject
import java.time.Instant

val EMBED_CHILDREN: Array<String> = arrayOf(
    "footer", "image", "thumbnail", "video", "provider", "author", "fields"
)

@Serializable
data class Embed(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val timestamp: String? = null,
    val color: String? = null,
    val footer: Footer? = null,
    val image: Image? = null,
    val thumbnail: Thumbnail? = null,
    val video: Video? = null,
    val provider: Provider? = null,
    val author: Author? = null,
    val fields: List<Field> = emptyList(),
) : Interpolatable<Embed> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Embed {
        return Embed(
            title = title?.let { interpolator.interpolate(it, context) },
            description = description?.let { interpolator.interpolate(it, context) },
            url = url?.let { interpolator.interpolate(it, context) },
            timestamp = timestamp?.let { interpolator.interpolate(it, context) },
            color = color?.let { interpolator.interpolate(it, context) },
            footer = footer?.interpolate(interpolator, context),
            image = image?.interpolate(interpolator, context),
            thumbnail = thumbnail?.interpolate(interpolator, context),
            video = video?.interpolate(interpolator, context),
            provider = provider?.interpolate(interpolator, context),
            author = author?.interpolate(interpolator, context),
            fields = fields.map { it.interpolate(interpolator, context) },
        )
    }
}

fun Embed.toMessageEmbed(): MessageEmbed = let {
    Embed {
        title = it.title
        description = it.description
        url = it.url
        timestamp = it.timestamp?.let { Instant.parse(it) }
        color = it.color?.toInt()
        it.footer?.let { footer(it.text, it.iconUrl) }
        image = it.image?.url
        thumbnail = it.thumbnail?.url
        it.author?.let { author(it.name, it.url, it.iconUrl) }
        it.fields.forEach { field(it.name, it.value, it.inline ?: true) }
    }
}

fun Embed.toDataObject() = DataObject.fromJson(Json.encodeToString(this))

fun Embed.applyTemplate(template: Embed): Embed {
    val title = this.title ?: template.title
    val description = this.description ?: template.description
    val url = this.url ?: template.url
    val timestamp = this.timestamp ?: template.timestamp
    val color = this.color ?: template.color
    val footer = this.footer ?: template.footer
    val image = this.image ?: template.image
    val thumbnail = this.thumbnail ?: template.thumbnail
    val author = this.author ?: template.author
    val fields = template.fields + this.fields

    return Embed(
        title, description, url, timestamp, color, footer,
        image, thumbnail, video, provider, author, fields
    )
}

@Serializable
data class Footer(
    val text: String,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("proxy_icon_url")
    val proxyIconUrl: String? = null,
) : Interpolatable<Footer> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Footer {
        return Footer(
            text = interpolator.interpolate(text, context),
            iconUrl = iconUrl?.let { interpolator.interpolate(it, context) },
            proxyIconUrl = proxyIconUrl?.let { interpolator.interpolate(it, context) },
        )
    }
}

@Serializable
data class Field(
    val name: String,
    val value: String,
    val inline: Boolean? = null,
) : Interpolatable<Field> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Field {
        return Field(
            name = interpolator.interpolate(name, context),
            value = interpolator.interpolate(value, context),
            inline = inline,
        )
    }
}

@Serializable
data class Author(
    val name: String,
    val url: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("proxy_icon_url")
    val proxyIconUrl: String? = null,
) : Interpolatable<Author> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Author {
        return Author(
            name = interpolator.interpolate(name, context),
            url = url?.let { interpolator.interpolate(it, context) },
            iconUrl = iconUrl?.let { interpolator.interpolate(it, context) },
            proxyIconUrl = proxyIconUrl?.let { interpolator.interpolate(it, context) },
        )
    }
}

@Serializable
data class Image(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
) : Interpolatable<Image> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Image {
        return Image(
            url = interpolator.interpolate(url, context),
            proxyUrl = proxyUrl?.let { interpolator.interpolate(it, context) },
            height = height,
            width = width,
        )
    }
}

@Serializable
data class Thumbnail(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
) : Interpolatable<Thumbnail> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Thumbnail {
        return Thumbnail(
            url = interpolator.interpolate(url, context),
            proxyUrl = proxyUrl?.let { interpolator.interpolate(it, context) },
            height = height,
            width = width,
        )
    }
}

@Serializable
data class Video(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
) : Interpolatable<Video> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Video {
        return Video(
            url = interpolator.interpolate(url, context),
            proxyUrl = proxyUrl?.let { interpolator.interpolate(it, context) },
            height = height,
            width = width,
        )
    }
}

@Serializable
data class Provider(
    val name: String? = null,
    val url: String? = null,
) : Interpolatable<Provider> {
    override fun interpolate(interpolator: Interpolator, context: InterpolationContext): Provider {
        return Provider(
            name = name?.let { interpolator.interpolate(it, context) },
            url = url?.let { interpolator.interpolate(url, context) },
        )
    }
}