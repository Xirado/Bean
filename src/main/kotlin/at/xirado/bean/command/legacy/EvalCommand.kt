package at.xirado.bean.command.legacy

import at.xirado.bean.Application
import at.xirado.bean.command.Arguments
import at.xirado.bean.command.LegacyCommand
import at.xirado.bean.coroutineScope
import at.xirado.bean.util.getLog
import at.xirado.bean.util.postHaste
import at.xirado.bean.util.queueSilently
import com.facebook.ktfmt.format.Formatter
import com.facebook.ktfmt.format.ParseError
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext

private val CODE_BLOCK_REGEX = "\\A```(?:kotlin|kt)?\\s+([\\s\\S]+)```\\Z".toRegex()

private val defaultImports = listOf(
    "kotlinx.coroutines.async",
    "dev.minn.jda.ktx.coroutines.await",
    "net.dv8tion.jda.api.managers.*",
    "net.dv8tion.jda.api.entities.*",
    "net.dv8tion.jda.api.*",
    "net.dv8tion.jda.api.utils.*",
    "net.dv8tion.jda.api.utils.data.*",
    "net.dv8tion.jda.internal.entities.*",
    "net.dv8tion.jda.internal.requests.*",
    "net.dv8tion.jda.api.requests.*",
    "java.io.*",
    "java.math.*",
    "java.util.*",
    "java.util.concurrent.*",
    "java.time.*",
    "at.xirado.bean.util.*"
)

private val ERROR_EMOJI = Emoji.fromCustom("error", 943524725487968298, false)
private val REPEAT_EMOJI = Emoji.fromCustom("repeat", 940204537355063346, false)
private val SOURCE_EMOJI = Emoji.fromUnicode("\uD83D\uDCDC")
private val RESULT_EMOJI = Emoji.fromUnicode("\uD83D\uDEE0")
private val DELETE_EMOJI = Emoji.fromUnicode("\uD83D\uDDD1")

private val EDIT_BUTTON = Button.primary("edit_and_rerun", "Edit & Re-run code").withEmoji(REPEAT_EMOJI)
private val DELETE_BUTTON = Button.danger("delete_eval_message", "Delete messages").withEmoji(DELETE_EMOJI)

class EvalCommand(override val app: Application) : LegacyCommand("eval") {
    init {
        devOnly = true
    }

    val engine: ScriptEngine = ScriptEngineManager().getEngineByName("kotlin")
    val mutex = Mutex()
    val cache = ExpiringMap.builder()
        .expiration(30, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.CREATED)
        .build<Long, KotlinEval>()

    override suspend fun execute(event: MessageReceivedEvent, args: Arguments) {
        if (args.isEmpty())
            return event.message.reply("Missing arguments!").queue()

        val code = CODE_BLOCK_REGEX.matchEntire(args.raw)?.groups?.get(1)?.value?.trim() ?: args.raw.trim()

        val invokeMessageId = event.messageIdLong

        val formatted = try {
            parse(code, defaultImports)
        } catch (ex: ParseError) {
            val exceptionHaste = postHaste(ex.stackTraceToString(), raw = false, extension = "txt")
            val exceptionString = ex.toString()
            val exceptionButton = errorButton(exceptionHaste)
            val message = event.message.reply("An exception occurred while parsing code!\n`${exceptionString}`")
                .setActionRow(exceptionButton, EDIT_BUTTON, DELETE_BUTTON)
                .await()
            cache[message.idLong] = KotlinEval(code, EvalState.PARSING_ERROR, invokeMessageId = invokeMessageId)
            return
        }

        val formattedHasteButton = coroutineScope.async { sourceButton(postHaste(formatted, extension = "kt")) }

        val bindings = mapOf(
            "scope" to coroutineScope,
            "channel" to event.channel,
            "guild" to event.guild,
            "jda" to event.jda,
            "user" to event.author,
            "author" to event.author,
            "member" to event.member!!,
            "api" to event.jda,
            "event" to event,
            "bot" to event.jda.selfUser,
            "selfUser" to event.jda.selfUser,
            "selfMember" to event.guild.selfMember,
            "log" to getLog<Application>(),
            "app" to app
        )

        val context = SimpleScriptContext()
        engine.context = context
        mutex.withLock {
            bindings.forEach { (t, u) -> engine.put(t, u) }
            val response = try {
                (engine.eval(formatted, context) as Deferred<*>).await()
            } catch (ex: Throwable) {
                val exceptionHaste = postHaste(ex.stackTraceToString(), raw = false, extension = "txt")
                val exceptionButton = errorButton(exceptionHaste)

                val message = event.message.reply("A runtime-exception occurred while evaluating script!\n`$ex`")
                    .setActionRow(exceptionButton, formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)
                    .await()

                cache[message.idLong] = KotlinEval(code, EvalState.RUNTIME_ERROR, invokeMessageId = invokeMessageId)
                return
            }

            if (response is Unit) {
                val message = event.message.reply("Code executed without errors!")
                    .setActionRow(formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)
                    .await()

                cache[message.idLong] = KotlinEval(code, EvalState.RETURN_UNIT, engine.context, invokeMessageId)
                return
            }

            val resultButton = if (response != null) resultButton(postHaste(response.toString())) else null

            val messageAction = event.message.reply("```\n${response?.toString() ?: "null"}```")

            if (resultButton != null)
                messageAction.setActionRow(resultButton, formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)
            else
                messageAction.setActionRow(formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)

            val message = messageAction.await()

            cache[message.idLong] = KotlinEval(code, EvalState.RETURN_VALUE, engine.context, invokeMessageId)
        }
    }

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is ButtonInteractionEvent -> {
                when (event.componentId) {
                    "edit_and_rerun" -> onEdit(event)
                    "delete_eval_message" -> onDelete(event)
                }
            }
            is ModalInteractionEvent -> {
                if (event.modalId.startsWith("eval:"))
                    onModal(event)
            }
        }
    }

    private fun onEdit(event: ButtonInteractionEvent) {
        if (event.user.idLong !in app.config.devUsers)
            return event.reply("This maze isn't meant for you!").setEphemeral(true).queue()
        val message = event.message
        val messageId = message.idLong

        val eval = cache[messageId] ?: return event.reply("Bindings for this eval are no longer saved!").setEphemeral(true).queue()

        val source = eval.source

        val input = TextInput.create("source_code", "Kotlin Source", TextInputStyle.PARAGRAPH)
            .setValue(source)
            .build()

        val modal = Modal.create("eval:$messageId", "Kotlin Eval")
            .addActionRow(input)
            .build()

        event.replyModal(modal).queue()
    }

    private fun onDelete(event: ButtonInteractionEvent) {
        if (event.user.idLong !in app.config.devUsers)
            return event.reply("This maze isn't meant for you!").setEphemeral(true).queue()

        val messageId = event.messageIdLong

        val state = cache[messageId]

        if (state != null) {
            if (state.invokeMessageId != null)
                event.channel.deleteMessageById(state.invokeMessageId).queueSilently()
            cache.remove(messageId)
        }

        event.message.delete().queue()
    }

    suspend fun onModal(event: ModalInteractionEvent) {
        val source = event.getValue("source_code")?.asString ?: return

        val messageId = event.modalId.split(":")[1].toLong()

        val eval = cache[messageId] ?: return event.reply("Bindings for this eval are no longer saved!").setEphemeral(true).queue()

        val formatted = try {
            parse(source, defaultImports)
        } catch (ex: ParseError) {
            val exceptionHaste = postHaste(ex.stackTraceToString(), raw = false, extension = "txt")
            val exceptionString = ex.toString()
            val exceptionButton = errorButton(exceptionHaste)

            val message = event.reply("An exception occurred while parsing code!\n`${exceptionString}`")
                .addActionRow(exceptionButton, EDIT_BUTTON, DELETE_BUTTON)
                .await().retrieveOriginal().await()

            cache[message.idLong] = KotlinEval(source, EvalState.PARSING_ERROR)
            return
        }

        val formattedHasteButton = coroutineScope.async { sourceButton(postHaste(formatted, extension = "kt")) }

        mutex.withLock {
            if (eval.context != null)
                engine.context = eval.context
            val response = try {
                (engine.eval(formatted) as Deferred<*>).await()
            } catch (ex: Throwable) {
                val exceptionHaste = postHaste(ex.stackTraceToString(), raw = false, extension = "txt")
                val exceptionButton = errorButton(exceptionHaste)

                val message = event.reply("A runtime-exception occurred while evaluating script!\n`$ex`")
                    .addActionRow(exceptionButton, formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)
                    .await().retrieveOriginal().await()

                cache[message.idLong] = KotlinEval(source, EvalState.RUNTIME_ERROR)
                return
            }

            if (response is Unit) {
                val message = event.reply("Code executed without errors!")
                    .addActionRow(formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)
                    .await().retrieveOriginal().await()

                cache[message.idLong] = KotlinEval(source, EvalState.RETURN_UNIT)
                return
            }

            val resultButton = if (response != null) resultButton(postHaste(response.toString())) else null

            val messageAction = event.reply("```\n${response?.toString() ?: "null"}```")

            if (resultButton != null)
                messageAction.addActionRow(resultButton, formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)
            else
                messageAction.addActionRow(formattedHasteButton.await(), EDIT_BUTTON, DELETE_BUTTON)

            val message = messageAction.await().retrieveOriginal().await()

            cache[message.idLong] = KotlinEval(source, EvalState.RETURN_VALUE, engine.context)
        }
    }
}


private fun parse(input: String, imports: List<String>): String {
    val split = input.split("\n")

    val toEval = mutableListOf<String>()

    val completeImports = mutableListOf<String>()
    completeImports.addAll(imports)

    split.forEach {
        if (it.startsWith("import ")) {
            val import = it.substring(7)
            completeImports.add(import)
            return@forEach
        }
        toEval.add(it)
    }

    val sb = StringBuilder()

    completeImports.forEach { sb.append("import $it\n") }

    sb.append("\n")

    sb.append("scope.async {\n")
    toEval.filter { it.isNotBlank() }.forEach { sb.append("$it\n") }
    sb.append("}")

    return Formatter.format(sb.toString(), removeUnusedImports = true)
}

private fun errorButton(url: String) = Button.link(url, "Stacktrace").withEmoji(ERROR_EMOJI)
private fun sourceButton(url: String) = Button.link(url, "Source-Code").withEmoji(SOURCE_EMOJI)
private fun resultButton(url: String) = Button.link(url, "Result").withEmoji(RESULT_EMOJI)

data class KotlinEval(val source: String, val state: EvalState, val context: ScriptContext? = null, val invokeMessageId: Long? = null)
enum class EvalState { PARSING_ERROR, RUNTIME_ERROR, RETURN_VALUE, RETURN_UNIT }