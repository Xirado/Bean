package at.xirado.bean.i18n

import at.xirado.bean.io.config.FileLoader
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.utils.data.DataArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

private val log = LoggerFactory.getLogger(LocalizationManager::class.java) as Logger

class LocalizationManager {

    private var locales = mutableMapOf<String, I18n>()

    val localeNames = mutableListOf<String>()

    init {
        val map = mutableMapOf<String, I18n>()

        try {
            val config = FileLoader.loadResourceAsYaml("i18n/locale_config.yml")

            config.optArray("locales").orElseGet(DataArray::empty)
                .stream(DataArray::getString)
                .map { it.trim() }
                .forEach(localeNames::add)
        } catch (ex: IOException) {
            log.error("Could not initialize localization manager", ex)
            throw ExceptionInInitializerError(ex)
        }

        localeNames.forEach { lang ->
            try {
                val file = FileLoader.loadResourceAsYaml("i18n/locales/$lang")
                val name = lang.removeSuffix(".yml")
                map[name] = I18n(name, file)
                log.info("Loaded locale $lang")
            } catch (ex: Exception) {
                log.error("Could not load locale $lang", ex)
            }
        }
        if ("en_US.yml" !in localeNames) {
            log.error("Default locale \"en_US\" was not found!")
        }
        locales = Collections.unmodifiableMap(map)
    }

    fun getForLanguageTag(tag: String): I18n {
        return locales[tag]?: locales["en_US"]!!
    }

    fun getForGuild(guild: Guild): I18n {
        return getForLanguageTag(guild.locale.toLanguageTag())
    }
}