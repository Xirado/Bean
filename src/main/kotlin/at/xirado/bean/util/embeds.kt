package at.xirado.bean.util

import at.xirado.bean.i18n.LocalizedMessage
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.InlineEmbed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.slf4j.Logger
import org.slf4j.LoggerFactory

enum class ResponseType(val key: String, val default: String, val iconUrl: String, val color: ColorPalette) {
    ERROR("general.header.error", "Error", "https://bean.bz/assets/icons/error.png", ColorPalette.DANGER),
    SUCCESS("general.header.success", "Success", "https://bean.bz/assets/icons/success.png", ColorPalette.SUCCESS),
    WARNING("general.header.warning", "Warning", "https://bean.bz/assets/icons/warning.png", ColorPalette.WARNING)
}

fun createResponseEmbed(responseType: ResponseType,
                        content: CharSequence,
                        builder: InlineEmbed.() -> Unit = {}): MessageEmbed {
    return Embed {
        if (content is LocalizedMessage) {
            author(
                name = content.i18n.get(responseType.key) ?: responseType.default,
                iconUrl = responseType.iconUrl
            )
        } else {
            author(name = responseType.default, iconUrl = responseType.iconUrl)
        }

        color = responseType.color.rgb
        description = content.toString()
        apply(builder)
    }
}


const val SUPPORT_GUILD_INVITE_URL = "https://discord.com/invite/7WEjttJtKa"
val BEAN_LOGO_EMOTE = Emoji.fromCustom("Bean", 922866602628743188L, false)
val SUPPORT_BUTTON = Button.link(SUPPORT_GUILD_INVITE_URL, BEAN_LOGO_EMOTE)

inline fun <reified T> getLog() = LoggerFactory.getLogger(T::class.java) as Logger

enum class ColorPalette(val rgb: Int) {
    DANGER(0xb50e14),
    WARNING(0xa1ad15),
    SUCCESS(0x3ae817),
    PRIMARY(0x1548ad),
    SECONDARY(0x222a42)
}