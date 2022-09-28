package at.xirado.bean.util

import at.xirado.bean.i18n.LocalizedMessageReference
import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration

suspend inline fun <reified T : GenericEvent> JDA.await(timeout: Duration, crossinline filter: (T) -> Boolean = { true }): T? {
    return withTimeoutOrNull(timeout) { await(filter) }
}

enum class ResponseType(val reference: LocalizedMessageReference?, val iconUrl: String?, val color: ColorPalette) {
    ERROR(LocalizedMessageReference("general.header.error"), "https://bean.bz/assets/embed/icons/error.jpg", ColorPalette.DANGER),
    SUCCESS(LocalizedMessageReference("general.header.success"), "https://bean.bz/assets/embed/icons/success.jpg", ColorPalette.SUCCESS),
    WARNING(LocalizedMessageReference("general.header.warning"), "https://bean.bz/assets/embed/icons/danger.jpg",  ColorPalette.WARNING),
    PRIMARY(null, null, ColorPalette.PRIMARY)
}

fun plainEmbed(type: ResponseType, header: CharSequence?, description: CharSequence, builder: InlineEmbed.() -> Unit = {}) = Embed {
    color = type.color.rgb
    author(name = header?.toString(), iconUrl = type.iconUrl)
    this.description = description.toString()
    apply(builder)
}

const val SUPPORT_GUILD_INVITE_URL = "https://discord.com/invite/7WEjttJtKa"
val BEAN_LOGO_EMOTE = Emoji.fromCustom("Bean", 922866602628743188L, false)
val SUPPORT_BUTTON = Button.link(SUPPORT_GUILD_INVITE_URL, BEAN_LOGO_EMOTE)

inline fun <reified T> getLog() = LoggerFactory.getLogger(T::class.java) as Logger

enum class ColorPalette(val rgb: Int) {
    DANGER(0xd75a4a),
    WARNING(0xff8900),
    SUCCESS(0x25ae88),
    PRIMARY(0x1548ad),
    SECONDARY(0x222a42)
}