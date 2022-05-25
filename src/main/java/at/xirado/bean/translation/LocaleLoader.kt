package at.xirado.bean.translation

import at.xirado.bean.data.LinkedDataObject
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

object LocaleLoader {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val languages = mutableListOf<String>()
    private val languageMap = mutableMapOf<String, LinkedDataObject>()

    init {
        this::class.java
            .getResourceAsStream("/assets/languages/list.txt")
            .use {
                val stream = it
                    ?: throw ExceptionInInitializerError("Could not initialize Language loader because list.txt does not exist!")
                stream.bufferedReader()
                    .readText()
                    .trim()
                    .lines()
                    .forEach(languages::add)
            }

        for (lang in languages) {
            this::class.java
                .getResourceAsStream("/assets/languages/$lang")
                .use {
                    val name = lang.removeSuffix(".json")
                    val json = LinkedDataObject.parse(it)
                    languageMap[name] = json.setMetadata(arrayOf(name))
                    logger.info("Successfully loaded locale $lang")
                }
        }
    }

    @JvmStatic
    fun getForLanguage(language: String) =
        languageMap[language] ?: languageMap.getValue("en_US")

    @JvmStatic
    fun ofGuild(guild: Guild) =
        getForLanguage(guild.locale.toLanguageTag())

    @JvmStatic
    fun parseDuration(seconds: Long, languageJSON: LinkedDataObject, delimiter: String): String {
        if (seconds == -1L) return languageJSON.getString("time.permanent")
        val days = TimeUnit.SECONDS.toDays(seconds)
        val hours = TimeUnit.SECONDS.toHours(seconds) - days * 24
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        val seconds1 = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60

        return buildString {
            if (days != 0L) {
                append("$days ")
                if (days == 1L) append(languageJSON.getString("time.day"))
                else append(languageJSON.getString("time.days"))
                append(delimiter)
            }

            if (hours != 0L) {
                append("$hours ")
                if (hours == 1L) append(languageJSON.getString("time.hour"))
                else append(languageJSON.getString("time.hours"))
                append(delimiter)
            }

            if (minutes != 0L) {
                append("$minutes ")
                if (minutes == 1L) append(languageJSON.getString("time.minute"))
                else append(languageJSON.getString("time.minutes"))
                append(delimiter)
            }

            if (seconds1 != 0L) {
                append("$seconds1 ")
                if (seconds1 == 1L) append(languageJSON.getString("time.second"))
                else append(languageJSON.getString("time.seconds"))
            }
        }.let { it.substring(0, it.length - delimiter.length) }
    }
}