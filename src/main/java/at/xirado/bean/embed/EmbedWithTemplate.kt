package at.xirado.bean.embed

import at.xirado.bean.interpolator.InterpolationContext
import at.xirado.bean.interpolator.Interpolator
import at.xirado.bean.model.*
import kotlinx.serialization.Serializable

@Serializable
data class EmbedWithTemplate(
    val template: String? = null,
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
) {
    val embed by lazy {
        Embed(
            title, description, url, timestamp, color, footer,
            image, thumbnail, video, provider, author, fields
        )
    }
}

fun EmbedWithTemplate.applyTemplate(template: Embed): Embed {
    return embed.applyTemplate(template)
}

fun EmbedWithTemplate.applyTemplate(
    template: Embed,
    interpolator: Interpolator,
    interpolationContext: InterpolationContext,
): Embed {
    return embed.interpolate(interpolator, interpolationContext).applyTemplate(template.interpolate(interpolator, interpolationContext))
}