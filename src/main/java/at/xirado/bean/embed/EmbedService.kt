package at.xirado.bean.embed

import at.xirado.bean.data.ResourceService
import at.xirado.bean.interpolator.InterpolationContext
import at.xirado.bean.interpolator.Interpolator
import at.xirado.bean.interpolator.namespace.I18nExpressionNamespace
import at.xirado.bean.interpolator.namespace.LOCALE_ENV_KEY
import at.xirado.bean.model.Embed
import at.xirado.bean.util.tomlToJsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

private val log = KotlinLogging.logger { }
private val json = Json {
    ignoreUnknownKeys = true
}

@Single
class EmbedService(
    private val resourceService: ResourceService,
) : KoinComponent {
    private val templates: Map<String, Embed> = readTemplates()
    private val embeds: Map<String, EmbedWithTemplate> = readEmbeds()
    private val interpolator by inject<Interpolator>()

    init {
        log.info { "Loaded ${embeds.size} embeds and ${templates.size} templates" }
    }

    fun getEmbed(
        name: String,
        interpolationContext: InterpolationContext? = null,
    ): Embed {
        val embed = embeds[name]
            ?: throw IllegalArgumentException("Embed \"$name\" not found")

        val templateName = embed.template

        if (interpolationContext != null) {
            return when {
                templateName == null -> embed.embed.interpolate(interpolator, interpolationContext)
                else -> {
                    val template = templates[templateName]
                        ?: throw IllegalStateException("Template \"$templateName\" does not exist! (Defined on \"$name\")")

                    embed.applyTemplate(template, interpolator, interpolationContext)
                }
            }
        }

        return when {
            templateName == null -> embed.embed
            else -> {
                val template = templates[templateName]
                    ?: throw IllegalStateException("Template \"$templateName\" does not exist! (Defined on \"$name\")")

                embed.applyTemplate(template)
            }
        }
    }

    fun getLocalizedMessageEmbed(name: String, locale: DiscordLocale, vararg arguments: Pair<String, Any?>): Embed {
        val context = InterpolationContext(
            env = mapOf(
                LOCALE_ENV_KEY to locale.locale
            ),
            namespaces = setOf(I18nExpressionNamespace::class),
            arguments = arguments.toMap(),
        )

        return getEmbed(name, context)
    }

    private fun readEmbeds(): Map<String, EmbedWithTemplate> {
        return resourceService.getResourceFilesRecursively(
            path = "embed",
            filter = { name != "templates.toml" && extension == "toml" }
        ) { path ->
            val content = resourceService.getFile(path).readText()
            val pathParts = path.map {
                if (it.isDirectory())
                    it.name
                else
                    it.nameWithoutExtension
            }.drop(1)

            val jsonObject = tomlToJsonObject(content)

            buildMap<String, EmbedWithTemplate> {
                jsonObject.forEach { (key, value) ->
                    if (value !is JsonObject)
                        return@forEach

                    val name = (pathParts + key).joinToString(separator = ".")
                    put(name, json.decodeFromJsonElement(value))
                }
            }
        }.reduce { acc, map -> acc + map }
    }

    private fun readTemplates(): Map<String, Embed> {
        return resourceService.getResourceFile("embed/templates.toml") { path ->
            val content = resourceService.getFile(path).readText()

            val jsonObject = tomlToJsonObject(content, json)

            buildMap {
                jsonObject.forEach { (key, value) ->
                    if (value !is JsonObject)
                        return@forEach

                    put(key, json.decodeFromJsonElement(value))
                }
            }
        }
    }
}