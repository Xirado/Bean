package at.xirado.bean.interaction.command.slash

import at.xirado.bean.Application
import at.xirado.bean.embed.EmbedBuilderState
import at.xirado.bean.embed.ZERO_WIDTH_SPACE
import at.xirado.bean.interaction.slash.AutoComplete
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.asNullableString
import at.xirado.bean.util.await
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import okhttp3.internal.toHexString
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

class BuildEmbedCommand(override val app: Application) : SlashCommand("embed-builder") {
    override var description: String = "Create and manage message-embeds"

    init {
        options {
            option<String>(name = "preset", description = "Modify a saved preset", autocomplete = true)
        }
        addUserPermissions(Permission.ADMINISTRATOR)
        addBotPermissions(Permission.MESSAGE_SEND)
    }

    private val cache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(10, TimeUnit.MINUTES)
        .build<String, EmbedBuilderState>()

    private fun extractIdentifier(message: Message): String? {
        val id = message.buttons
            .find { it.id?.startsWith("embed_builder:send") ?: false }
            ?.id
            ?: return null

        return id.substring(id.indexOf(":", id.indexOf(":") + 1) + 1)
    }

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent, preset: String?) {
        val identifier = event.interaction.id
        val state = if (preset != null) {
            getPreset(event.guild!!.idLong, preset)
                ?.let { EmbedBuilderState.fromPreset(preset, it, identifier , event.jda) }
                ?: EmbedBuilderState(event.jda, identifier)
        } else {
            EmbedBuilderState(event.jda, identifier)
        }

        cache[identifier] = state

        event.reply(state.getContent())
            .apply {
                if (state.presetName != null)
                    addEmbeds(state.buildEmbed())
            }
            .setEphemeral(true)
            .addComponents(state.getLayout())
            .queue()
    }

    @AutoComplete("preset")
    suspend fun onAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val presets = getPresetNames(event.guild!!.idLong)
            .filter { it.startsWith(event.focusedOption.value, ignoreCase = true) }
            .take(25)
            .map { Command.Choice(it, it) }

        event.replyChoices(presets).queue()
    }

    private suspend fun onSave(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        if (state.presetName != null) {
            addPreset(event.guild!!.idLong, state.presetName!!, DataArray.fromCollection(state.buildAllEmbeds()).toString())
            return state.update(event, additionalMessage = "✅ Preset \"${state.presetName}\" has been saved!")
        }

        val id = "embed_builder:save_preset:${event.interaction.idLong}"
        val modal = Modal.create(id, "Save preset")
            .addActionRow(TextInput.create("name", "Preset-Name", TextInputStyle.SHORT)
                .setRequiredRange(5, 64)
                .setPlaceholder("Very cool Embed")
                .build()
            )
            .build()

        event.replyModal(modal).queue()

        val modalEvent = event.jda.await<ModalInteractionEvent>(5.minutes) { it.modalId == id } ?: return
        val name = modalEvent.getValue("name")!!.asString
        if (getPreset(modalEvent.guild!!.idLong, name) != null)
            return state.update(modalEvent, additionalMessage = "❌ A Preset named \"$name\" already exists!")

        addPreset(modalEvent.guild!!.idLong, name, DataArray.fromCollection(state.buildAllEmbeds()).toString())
        state.presetName = name
        state.update(modalEvent, additionalMessage = "✅ Preset \"$name\" has been saved!", components = true)
    }

    private suspend fun send(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        event.channel.sendMessageEmbeds(state.buildAllEmbeds()).await()
        event.deferEdit().queue()
    }

    private fun onDescriptionButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed
        val value = embed.descriptionBuilder.toString().let { if (it == ZERO_WIDTH_SPACE || it.isEmpty()) null else it }

        val modal = Modal.create("embed_builder:description", "Change description")
            .addActionRow(
                TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                    .setValue(value)
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetDescription(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val description = event.getValue("description")!!.asString.ifEmpty { ZERO_WIDTH_SPACE }
        state.modifyCurrentEmbed { setDescription(description) }
        state.update(event, embed = true)
    }

    private fun onTitleButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:title", "Change title")
            .addActionRow(
                TextInput.create("title", "Title", TextInputStyle.SHORT)
                    .setValue(embed.title)
                    .setRequired(false)
                    .build()
            )
            .addActionRow(
                TextInput.create("url", "URL", TextInputStyle.SHORT)
                    .setValue(embed.url)
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetTitle(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val title = event.getValue("title")!!.asNullableString
        val url = event.getValue("url")!!.asNullableString
        state.modifyCurrentEmbed { setTitle(title, url) }
        state.update(event, embed = true)
    }

    private fun onFooterButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:footer", "Change footer")
            .addActionRow(
                TextInput.create("footer", "Footer", TextInputStyle.SHORT)
                    .setValue(embed.footer?.text)
                    .setRequired(false)
                    .build()
            )
            .addActionRow(
                TextInput.create("url", "Image-URL", TextInputStyle.SHORT)
                    .setValue(embed.footer?.iconUrl)
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetFooter(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val footer = event.getValue("footer")!!.asNullableString
        val url = event.getValue("url")!!.asNullableString
        state.modifyCurrentEmbed { setFooter(footer, url) }
        state.update(event, embed = true)
    }

    private fun onJsonButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:from_json", "Read JSON text")
            .addActionRow(
                TextInput.create("json", "JSON", TextInputStyle.PARAGRAPH)
                    .setValue(embed.toData().toString())
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetJson(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val json = event.getValue("json")!!.asString
        val embedObject = try {
            DataObject.fromJson(json).put("type", "rich")
        } catch (throwable: Throwable) {
            return state.update(event, additionalMessage = "❌ Invalid JSON-Format!")
        }
        val embed = (event.jda as JDAImpl).entityBuilder.createMessageEmbed(embedObject)
        state.selectedEmbed = EmbedBuilder(embed)
        state.update(event, embed = true)
    }

    private fun onFieldButton(event: ButtonInteractionEvent) {
        val modal = Modal.create("embed_builder:add_field", "Add field")
            .addActionRow(
                TextInput.create("name", "Name", TextInputStyle.SHORT)
                    .build()
            )
            .addActionRow(
                TextInput.create("value", "Content", TextInputStyle.PARAGRAPH)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onAddField(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val name = event.getValue("name")!!.asString
        val value = event.getValue("value")!!.asString
        state.modifyCurrentEmbed { addField(name, value, false) }
        state.update(event, embed = true)
    }

    private fun onAuthorButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:author", "Change author")
            .addActionRow(
                TextInput.create("name", "Name", TextInputStyle.SHORT)
                    .setValue(embed.author?.name)
                    .setRequired(false)
                    .build()
            )
            .addActionRow(
                TextInput.create("icon-url", "Image-URL", TextInputStyle.SHORT)
                    .setValue(embed.author?.iconUrl)
                    .setRequired(false)
                    .build()
            )
            .addActionRow(
                TextInput.create("url", "URL", TextInputStyle.SHORT)
                    .setValue(embed.author?.url)
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetAuthor(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val name = event.getValue("name")!!.asNullableString
        val iconUrl = event.getValue("icon-url")!!.asNullableString
        val url = event.getValue("url")!!.asNullableString
        state.modifyCurrentEmbed { setAuthor(name, url, iconUrl) }
        state.update(event, embed = true)
    }

    private fun onColorButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:color", "Change color")
            .addActionRow(
                TextInput.create("color", "Color", TextInputStyle.SHORT)
                    .setValue(embed.colorRaw.toHexString().uppercase())
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetColor(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val color = event.getValue("color")!!.asString.let { Integer.parseInt(it, 16) }
        state.modifyCurrentEmbed { setColor(color) }
        state.update(event, embed = true)
    }

    private fun onImageButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:image", "Change image")
            .addActionRow(
                TextInput.create("image", "Image-URL", TextInputStyle.SHORT)
                    .setValue(embed.image?.url)
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetImage(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val image = event.getValue("image")!!.asNullableString
        state.modifyCurrentEmbed { setImage(image) }
        state.update(event, embed = true)
    }

    private fun onThumbnailButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val embed = state.selectedEmbed.build()

        val modal = Modal.create("embed_builder:thumbnail", "Change thumbnail")
            .addActionRow(
                TextInput.create("thumbnail", "Image-URL", TextInputStyle.SHORT)
                    .setValue(embed.thumbnail?.url)
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    private suspend fun onSetThumbnail(event: ModalInteractionEvent, state: EmbedBuilderState) {
        val thumbnail = event.getValue("thumbnail")!!.asNullableString
        state.modifyCurrentEmbed { setThumbnail(thumbnail) }
        state.update(event, embed = true)
    }

    private suspend fun onDelete(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        val presetName = state.presetName!!
        deletePreset(event.guild!!.idLong, presetName)
        state.presetName = null
        state.update(event, additionalMessage = "✅ Preset \"$presetName\" has been deleted!", components = true)
    }

    private suspend fun onNavigateLeft(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        state.selection--
        state.update(event, embed = true, components = true)
    }

    private suspend fun onNavigateRight(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        state.selection++
        state.update(event, embed = true, components = true)
    }

    private suspend fun onAddEmbed(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        state.addNewEmbed(null)
        state.update(event, embed = true, components = true)
    }

    private suspend fun onRemoveEmbed(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        state.removeEmbed(state.selection)
        if (state.selection > 1)
            state.selection--
        state.update(event, embed = true, components = true)
    }

    private fun onPreviewButton(event: ButtonInteractionEvent, state: EmbedBuilderState) {
        event.replyEmbeds(state.buildAllEmbeds())
            .setEphemeral(true)
            .queue()
    }

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is ModalInteractionEvent -> {
                val identifier = event.message?.let { extractIdentifier(it) } ?: return
                val state = cache[identifier] ?: return event.reply("Request timed out! Please try again.").setEphemeral(true).queue()

                when (event.modalId) {
                    "embed_builder:thumbnail"   -> onSetThumbnail(event, state)
                    "embed_builder:image"       -> onSetImage(event, state)
                    "embed_builder:color"       -> onSetColor(event, state)
                    "embed_builder:author"      -> onSetAuthor(event, state)
                    "embed_builder:add_field"   -> onAddField(event, state)
                    "embed_builder:from_json"   -> onSetJson(event, state)
                    "embed_builder:footer"      -> onSetFooter(event, state)
                    "embed_builder:title"       -> onSetTitle(event, state)
                    "embed_builder:description" -> onSetDescription(event, state)
                }
            }

            is ButtonInteractionEvent -> {
                val identifier = extractIdentifier(event.message) ?: return
                val state = cache[identifier] ?: return event.reply("Request timed out! Please try again.").setEphemeral(true).queue()
                when (event.componentId) {
                    "embed_builder:preview"                         -> onPreviewButton(event, state)
                    "embed_builder:remove"                          -> onRemoveEmbed(event, state)
                    "embed_builder:add"                             -> onAddEmbed(event, state)
                    "embed_builder:right"                           -> onNavigateRight(event, state)
                    "embed_builder:left"                            -> onNavigateLeft(event, state)
                    "embed_builder:thumbnail"                       -> onThumbnailButton(event, state)
                    "embed_builder:image"                           -> onImageButton(event, state)
                    "embed_builder:color"                           -> onColorButton(event, state)
                    "embed_builder:author"                          -> onAuthorButton(event, state)
                    "embed_builder:add_field"                       -> onFieldButton(event)
                    "embed_builder:from_json"                       -> onJsonButton(event, state)
                    "embed_builder:footer"                          -> onFooterButton(event, state)
                    "embed_builder:title"                           -> onTitleButton(event, state)
                    "embed_builder:description"                     -> onDescriptionButton(event, state)
                    "embed_builder:save_preset"                     -> onSave(event, state)
                    "embed_builder:save_preset:${state.presetName}" -> onSave(event, state)
                    "embed_builder:delete:${state.presetName}"      -> onDelete(event, state)
                    "embed_builder:send:$identifier"                -> send(event, state)
                }
            }
        }
    }
}

private suspend fun getPresetNames(guildId: Long): List<String> {
    return SQLBuilder("SELECT name FROM embed_presets WHERE guild_id = ?", guildId).executeQuery {
        val list = mutableListOf<String>()
        while (it.next()) list.add(it.getString("name"))
        return@executeQuery list
    } ?: emptyList()
}

private suspend fun getPreset(guildId: Long, name: String): String? {
    return SQLBuilder("SELECT embed FROM embed_presets WHERE guild_id = ? AND name = ?", guildId, name).executeQuery {
        if (it.next())
            return@executeQuery it.getString("embed")
        return@executeQuery null
    }
}

private suspend fun deletePreset(guildId: Long, name: String) {
    SQLBuilder("DELETE FROM embed_presets WHERE guild_id = ? AND name = ?", guildId, name).execute()
}

private suspend fun addPreset(guildId: Long, name: String, value: String) {
    SQLBuilder("INSERT INTO embed_presets values(?, ?, ?::JSONB) ON CONFLICT(guild_id, name) DO UPDATE SET embed = excluded.embed", guildId, name, value).execute()
}