package at.xirado.bean.i18n

import at.xirado.bean.io.config.FileLoader
import net.dv8tion.jda.api.utils.data.DataArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

class LocalizationManager {

    companion object {
        private val log = LoggerFactory.getLogger(LocalizationManager::class.java) as Logger
        private var LANGUAGE_MAP = mutableMapOf<String, I18n>()

        val LANGUAGES = mutableListOf<String>()


        init {
            val map = mutableMapOf<String, I18n>()

            try {
                val config = FileLoader.loadResourceAsYaml("i18n/locale_config.yml")

                config.optArray("locales").orElseGet(DataArray::empty)
                    .stream(DataArray::getString)
                    .map { it.trim() }
                    .forEach(LANGUAGES::add)
            } catch (ex: IOException) {
                log.error("Could not initialize localization manager", ex)
                throw ExceptionInInitializerError(ex)
            }

            LANGUAGES.forEach { lang ->
                try {
                    val file = FileLoader.loadResourceAsYaml("i18n/locales/$lang")
                    val name = lang.replace(".yml", "")
                    map[name] = I18n(name, file)
                    log.info("Loaded locale $lang")
                } catch (ex: Exception) {
                    log.error("Could not load locale $lang", ex)
                }
            }
            LANGUAGE_MAP = Collections.unmodifiableMap(map)
        }

        fun getForLanguageTag(tag: String): I18n? {
            return LANGUAGE_MAP[tag]
        }
    }
}