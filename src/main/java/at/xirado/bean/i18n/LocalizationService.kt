package at.xirado.bean.i18n

import at.xirado.bean.data.ResourceService
import at.xirado.bean.util.tomlToJsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.koin.core.annotation.Single
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

private val log = KotlinLogging.logger { }

private val englishUS = Locale.forLanguageTag("en-US")

@Single(createdAtStart = true)
class LocalizationService {
    private val languages = loadLocales().associateBy { it.locale }

    init {
        if (languages.none { it.key == englishUS })
            log.error { "Locale file en_US not loaded! This will cause problems!" }
    }

    fun getString(locale: Locale, key: String, vararg arguments: Any): String {
        val language = languages[locale]

        if (language == null) {
            if (locale == englishUS)
                throw IllegalStateException("Tried getting localized string from en_US but it was not there!")

            return getString(englishUS, key, *arguments)
        }

        val string = language.getString(key, *arguments)

        if (string == null) {
            if (locale == englishUS)
                throw IllegalStateException("Localized string \"$key\" not present in english locale file!")

            return getString(englishUS, key, *arguments)
        }

        return string
    }

    fun getString(locale: DiscordLocale, key: String, vararg arguments: Any): String {
        return getString(locale.toLocale(), key, *arguments)
    }

    private fun loadLocales(): List<Language> = ResourceService.getResourceFilesRecursively(
        path = "locale",
        filter = { extension == "toml" },
    ) { path ->
        val localeName = path.nameWithoutExtension
        val locale = Locale.forLanguageTag(localeName)

        if (locale == null) {
            log.error { "Locale file $localeName is not a valid locale!" }
            return@getResourceFilesRecursively null
        }

        val discordLocale = DiscordLocale.from(locale)

        if (discordLocale == DiscordLocale.UNKNOWN)
            log.warn { "Locale $localeName has no entry in DiscordLocale" }

        try {
            val content = ResourceService.getFile(path).readText()
            val localeObj = tomlToJsonObject(content)
            val localeMap = localeObj.extractStrings()
            val discordLocaleOrNull = if (discordLocale != DiscordLocale.UNKNOWN) discordLocale else null

            Language(localeName, locale, discordLocaleOrNull, localeMap)
        } catch (e: Exception) {
            log.error(e) { "Failed to load locale $localeName" }
            return@getResourceFilesRecursively null
        }
    }

    private fun JsonObject.extractStrings(prefix: String = ""): Map<String, String> = buildMap {
        val realPrefix = if (prefix.isEmpty()) "" else "$prefix."

        this@extractStrings.forEach { (key, value) ->
            when (value) {
                is JsonPrimitive -> {
                    if (!value.isString)
                        return@forEach
                    val content = value.contentOrNull ?: return@forEach
                    put("$realPrefix$key", content)
                }
                is JsonObject -> {
                    val newPrefix = "$realPrefix$key"
                    putAll(value.extractStrings(newPrefix))
                }
                else -> {}
            }
        }
    }
}