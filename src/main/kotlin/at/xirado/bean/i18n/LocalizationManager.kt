package at.xirado.bean.i18n

import at.xirado.bean.io.config.FileLoader
import at.xirado.bean.util.getLog
import at.xirado.bean.util.getNullable
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.collect
import net.dv8tion.jda.api.interactions.DiscordLocale
import okhttp3.internal.toImmutableMap
import java.io.IOException
import java.util.*

private val log = getLog<LocalizationManager>()

class LocalizationManager {

    val messageReferences = Collections.synchronizedList(mutableListOf<LocalizedMessageReference>())

    val locales: Map<String, I18n>
    val default: I18n

    init {
        val map = mutableMapOf<String, I18n>()

        try {
            val config = FileLoader.loadResourceAsYaml("i18n/locale_config.yml")

            default = config.getNullable<String>("default")
                ?.let { loadLocale(it) } ?: throw IllegalStateException("No default locale defined!")

            map[default.tag] = default
            log.info("Registered default-locale ${default.tag}!")
            config.getNullable<JSONArray>("locales")
                ?.collect<String>()
                ?.mapNotNull { file ->
                    kotlin.runCatching {
                        loadLocale(file.trim())
                    }.onFailure {
                        log.error("Failed to load translation-file \"$file\"!", it)
                    }.onSuccess {
                        log.info("Registered locale ${it.tag}!")
                    }.getOrNull() }
                ?.forEach { map[it.tag] = it }

            locales = map.toImmutableMap()
        } catch (ex: IOException) {
            log.error("Could not initialize localization manager", ex)
            throw ExceptionInInitializerError(ex)
        }
    }

    private fun loadLocale(fileName: String): I18n {
        val file = FileLoader.loadResourceAsYaml("i18n/locales/$fileName")
        val name = fileName.removeSuffix(".yml")
        return I18n(name, fileName, file, this)
    }

    fun getForLanguageTag(tag: String): I18n {
        return locales[tag]?: default
    }

    fun getDiscordLocalizations(path: String, vararg attributes: Pair<String, Any>): Map<DiscordLocale, String> {
        val map = mutableMapOf<DiscordLocale, String>()
        locales.forEach { (name, locale) ->
            val discordLocale = DiscordLocale.from(name)

            if (discordLocale == DiscordLocale.UNKNOWN)
                return@forEach

            val result = locale.get(path, *attributes) ?: return@forEach

            map[discordLocale] = result
        }

        return map
    }
}