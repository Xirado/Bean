package at.xirado.bean.util

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val ERROR_EMOTE = "<:error:943524725487968298>"

const val SUPPORT_GUILD_INVITE_URL = "https://discord.com/invite/7WEjttJtKa"
val BEAN_LOGO_EMOTE = Emoji.fromEmote("Bean", 922866602628743188L, false)
val SUPPORT_BUTTON = Button.link(SUPPORT_GUILD_INVITE_URL, BEAN_LOGO_EMOTE)

fun errorEmbed(message: String) = Embed {
    description = "$ERROR_EMOTE $message"
    color = ColorPalette.DANGER.rgb
}

inline fun <reified T> getLog() = LoggerFactory.getLogger(T::class.java) as Logger

enum class ColorPalette(val rgb: Int) {
    DANGER(0xb50e14),
    WARNING(0xa1ad15),
    PRIMARY(0x1548ad),
    SECONDARY(0x222a42)
}