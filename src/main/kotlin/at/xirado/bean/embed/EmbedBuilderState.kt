package at.xirado.bean.embed

import at.xirado.bean.util.disableIf
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.internal.JDAImpl

const val ZERO_WIDTH_SPACE = "\u200B"

val BUTTON_FROM_JSON = Button.primary("embed_builder:from_json", "Read from JSON")
val BUTTON_ADD_FIELD = Button.secondary("embed_builder:add_field", "Add field")
val BUTTON_SET_AUTHOR = Button.secondary("embed_builder:author", "Author")
val BUTTON_SET_FOOTER = Button.secondary("embed_builder:footer", "Footer")
val BUTTON_SET_TITLE = Button.secondary("embed_builder:title", "Title")
val BUTTON_SET_IMAGE = Button.secondary("embed_builder:image", "Image")
val BUTTON_SET_THUMBNAIL = Button.secondary("embed_builder:thumbnail", "Thumbnail")
val BUTTON_SET_COLOR = Button.secondary("embed_builder:color", "Color")
val BUTTON_SET_DESCRIPTION = Button.secondary("embed_builder:description", "Description")
val BUTTON_NAVIGATE_LEFT = Button.primary("embed_builder:left", Emoji.fromUnicode("⬅"))
val BUTTON_NAVIGATE_RIGHT = Button.primary("embed_builder:right", Emoji.fromUnicode("➡"))
val BUTTON_ADD_EMBED = Button.success("embed_builder:add", "Add")
val BUTTON_REMOVE_EMBED = Button.danger("embed_builder:remove", "Remove")
val BUTTON_PREVIEW = Button.secondary("embed_builder:preview", "Preview")

fun getPresetButton(preset: String?): Button {
    return if (preset == null)
        Button.primary("embed_builder:save_preset", "Save")
    else
        Button.primary("embed_builder:save_preset:$preset", "Save \"$preset\"")
}

fun getSendButton(identifier: String) =
    Button.success("embed_builder:send:$identifier", "Send")

fun getDeletePresetButton(preset: String) =
    Button.danger("embed_builder:delete:$preset", "Delete \"$preset\"")

class EmbedBuilderState(val jda: JDA, val identifier: String, var presetName: String? = null) {
    var message = "Press **${BUTTON_ADD_EMBED.label}** to get started"
    private val embeds = mutableListOf<EmbedBuilder>()
    val embedCount: Int
        get() = embeds.size
    var selection: Int = 0
        set(value) {
            field = if (value > embeds.size)
                embeds.size
            else if (embeds.isNotEmpty() && value < 1)
                1
            else if (embeds.isEmpty() && value < 0)
                0
            else
                value
        }
    var selectedEmbed: EmbedBuilder
        get() {
            if (selection == 0)
                throw IllegalStateException("No Embed selected!")
            return embeds[selection - 1]
        }
        set(value) {
            embeds[selection - 1] = value
        }

    fun getContent(additionalMessage: String? = null): String {
        val page = IntRange(1, embeds.size).joinToString(separator = " ") { if (it == selection) "__**$it**__" else it.toString() }
        return buildString {
            if (embeds.isEmpty())
                append("Press **${BUTTON_ADD_EMBED.label}** to get started")
            else
                append("⚒ - $page")
            if (additionalMessage != null)
                append("\n\n$additionalMessage")
        }
    }

    suspend fun update(event: ButtonInteractionEvent, additionalMessage: String? = null, embed: Boolean = false, components: Boolean = false) {
        val embedBuilders = embeds
        event.editMessage(getContent(additionalMessage)).apply {
            if (embed)
                if (embedBuilders.isEmpty())
                    setEmbeds(emptyList())
                else
                    setEmbeds(buildEmbed())
            if (components)
                setComponents(getLayout())
        }.await()
    }

    suspend fun update(event: ModalInteractionEvent, additionalMessage: String? = null, embed: Boolean = false, components: Boolean = false) {
        val embedBuilders = embeds
        event.editMessage(getContent(additionalMessage)).apply {
            if (embed)
                if (embedBuilders.isEmpty())
                    setEmbeds(emptyList())
                else
                    setEmbeds(buildEmbed())
            if (components)
                setComponents(getLayout())
        }.await()
    }

    fun getLayout() = listOf(
        ActionRow.of(BUTTON_SET_DESCRIPTION, BUTTON_SET_TITLE, BUTTON_SET_COLOR, BUTTON_SET_IMAGE).disableIf { embeds.isEmpty() },
        ActionRow.of(BUTTON_SET_THUMBNAIL, BUTTON_SET_FOOTER, BUTTON_SET_AUTHOR, BUTTON_ADD_FIELD).disableIf { embeds.isEmpty() },
        ActionRow.of(
            BUTTON_NAVIGATE_LEFT.disableIf { selection <= 1 },
            BUTTON_NAVIGATE_RIGHT.disableIf { selection >= embeds.size },
            BUTTON_ADD_EMBED.disableIf { embeds.size >= 5 },
            BUTTON_REMOVE_EMBED.disableIf { embeds.size == 0 }
        ),
        if (presetName != null)
            ActionRow.of(
                getDeletePresetButton(presetName!!),
                getPresetButton(presetName!!).disableIf { embeds.isEmpty() },
                BUTTON_FROM_JSON.disableIf { embeds.isEmpty() },
                getSendButton(identifier).disableIf { embeds.isEmpty() },
                BUTTON_PREVIEW.disableIf { embeds.isEmpty() }
            )
        else
            ActionRow.of(
                getPresetButton(null).disableIf { embeds.isEmpty() },
                BUTTON_FROM_JSON.disableIf { embeds.isEmpty() },
                getSendButton(identifier).disableIf { embeds.isEmpty() },
                BUTTON_PREVIEW.disableIf { embeds.isEmpty() }
            )
    )

    fun buildEmbed() = embeds[selection - 1].build()

    fun buildAllEmbeds() = embeds.map(EmbedBuilder::build)

    fun removeEmbed(selection: Int) {
        embeds.removeAt(selection - 1)
    }

    fun addNewEmbed(embed: MessageEmbed? = null, position: Int = selection) {
        var pos: Int = position
        if (embeds.isEmpty())
            pos = 0
        if (pos > 0) {
            embeds.add(position, embed?.let { EmbedBuilder(it) } ?: EmbedBuilder().setDescription(ZERO_WIDTH_SPACE) )
        } else {
            embeds.add(embed?.let { EmbedBuilder(it) } ?: EmbedBuilder().setDescription(ZERO_WIDTH_SPACE))
        }
        selection++
    }

    fun modifyCurrentEmbed(block: EmbedBuilder.() -> Unit) {
        if (selection == 0)
            throw IllegalStateException("No embed selected!")
        embeds[selection - 1].apply(block)
    }

    companion object {
        fun fromPreset(name: String, jsonString: String, identifier: String, jda: JDA): EmbedBuilderState {
            val embeds = DataArray.fromJson(jsonString)
                .stream(DataArray::getObject)
                .map { (jda as JDAImpl).entityBuilder.createMessageEmbed(it.put("type", "rich")) }

            val state = EmbedBuilderState(jda, identifier, name)
            embeds.forEach { state.addNewEmbed(it) }
            return state
        }
    }
}