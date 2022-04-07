package at.xirado.bean.i18n

import net.dv8tion.jda.api.utils.data.DataObject
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

class LocalizationManager {

    companion object {
        private val log = LoggerFactory.getLogger(LocalizationManager::class.java) as Logger
        private var LANGUAGE_MAP = mutableMapOf<String, I18n>()

        val LANGUAGES = mutableListOf<String>()


        init {
            val map = mutableMapOf<String, I18n>()

            try {
                LocalizationManager::class.java.getResourceAsStream("/i18n/languages.txt").use { inputStream ->
                    if (inputStream == null) {
                        throw ExceptionInInitializerError("languages.txt doesn't exist!")
                    }

                    IOUtils.toString(inputStream, StandardCharsets.UTF_8).trim().split("\n").forEach {
                        val language = it.trim()
                        LANGUAGES.add(language)
                    }
                }
            } catch (ex: IOException) {
                log.error("Could not initialize localization manager", ex)
                throw ExceptionInInitializerError(ex)
            }

            LANGUAGES.forEach { lang ->
                try {
                    LocalizationManager::class.java.getResourceAsStream("/i18n/$lang").use {
                        if (it == null) {
                            log.warn("Ignoring missing locale file $lang")
                            return@forEach
                        }

                        val name = lang.replace(".yml", "")
                        val json = DataObject.fromYaml(it)
                        map[name] = I18n(name, json)
                        log.info("Loaded locale $lang")

                    }
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