package at.xirado.bean.util

import at.xirado.bean.i18n.LocalizedMessageReference
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.InlineEmbed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.messages.SendDefaults.ephemeral
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.internal.interactions.InteractionHookImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

enum class ResponseType(val reference: LocalizedMessageReference?, val iconUrl: String?, val color: ColorPalette) {
    ERROR(LocalizedMessageReference.of("general.header.error"), "https://bean.bz/assets/embed/icons/error.jpg", ColorPalette.DANGER),
    SUCCESS(LocalizedMessageReference.of("general.header.success"), "https://bean.bz/assets/embed/icons/success.jpg", ColorPalette.SUCCESS),
    WARNING(LocalizedMessageReference.of("general.header.warning"), "https://bean.bz/assets/embed/icons/danger.jpg",  ColorPalette.WARNING),
    PRIMARY(null, null, ColorPalette.PRIMARY)
}

suspend fun SlashCommandInteractionEvent.send(type: ResponseType = ResponseType.PRIMARY,
                                              message: LocalizedMessageReference,
                                              vararg attributes: Pair<String, Any> = emptyArray(),
                                              ephemeral: Boolean = false, builder: InlineEmbed.() -> Unit = {}) {
    val i18n = if (ephemeral) getUserI18n() else getGuildI18n()
    val embed = plainEmbed(type, type.reference?.get(i18n), message.get(i18n, *attributes), builder)
    if (isAcknowledged)
        hook.sendMessageEmbeds(embed).setEphemeral(ephemeral).await()
    else
        replyEmbeds(embed).setEphemeral(ephemeral).await()
}

fun InteractionHookImpl.isEphemeral() = ephemeral

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