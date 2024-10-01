package at.xirado.bean.i18n

import net.dv8tion.jda.api.interactions.DiscordLocale
import java.text.MessageFormat
import java.util.*

data class Language(
    val name: String,
    val locale: Locale,
    val discordLocale: DiscordLocale?,
    private val localeMap: Map<String, String>
) {
    fun getString(key: String, vararg arguments: Any): String? {
        val raw = localeMap[key]
            ?: return null

        val messageFormat = MessageFormat(raw, locale)
        return messageFormat.format(arguments)
    }
}