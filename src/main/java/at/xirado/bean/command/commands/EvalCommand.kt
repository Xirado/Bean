package at.xirado.bean.command.commands

import at.xirado.bean.Bean
import at.xirado.bean.command.Command
import at.xirado.bean.command.CommandContext
import at.xirado.bean.command.CommandFlag
import at.xirado.bean.ktx.await
import at.xirado.bean.misc.Hastebin
import com.facebook.ktfmt.format.Formatter
import com.facebook.ktfmt.format.ParseError
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Deferred
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.script.ScriptEngineManager
import kotlin.time.Duration.Companion.minutes

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
    "java.time.*"
)

class EvalCommand : Command("eval", "evaluates some code", "eval [code]") {

    private val engine = ScriptEngineManager().getEngineByName("kotlin")

    init {
        addCommandFlags(CommandFlag.DEVELOPER_ONLY)
    }

    override suspend fun executeCommand(event: MessageReceivedEvent, context: CommandContext) {
        val message = event.message
        val input = context.arguments.rawArguments

        if (input == null) {
            message.reply("Error: missing arguments!").mentionRepliedUser(false).queue()
            return
        }

        val raw = if (input.startsWith("```") && input.endsWith("```")) {
            input.substring(input.indexOf("\n"), input.length - 3)
        } else {
            input
        }.trim()

        val bindings = mapOf(
            "scope" to Bean.getInstance().commandHandler.scope,
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
            "log" to LoggerFactory.getLogger(Bean::class.java) as Logger
        )

        bindings.forEach { (t, u) -> engine.put(t, u) }

        val formatted = try {
            parse(raw, defaultImports)
        } catch (ex: ParseError) {
            val error = ex.toString()
            message.reply("An error occurred while formatting the code!" + if (error.length > 1000) "" else "\n```\n$error```")
                .mentionRepliedUser(false)
                .setActionRow(
                    generateError(ex.toString()),
                    generateSource(raw),
                    editCodeButton(event.messageIdLong),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(event.jda, event.messageIdLong, raw, bindings)
            return
        }

        val response = try {
            (engine.eval(formatted) as Deferred<*>).await()
        } catch (ex: Exception) {
            val error = ex.toString()
            message.reply("An error occurred while running the Kotlin script!" + if (error.length > 1000) "" else "\n```\n$error```")
                .mentionRepliedUser(false)
                .setActionRow(
                    generateError(ex.toString()),
                    generateSource(formatted),
                    editCodeButton(event.messageIdLong),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(event.jda, event.messageIdLong, raw, bindings)
            return
        }

        if (response is Unit) {
            message.reply("Code executed without errors")
                .mentionRepliedUser(false)
                .setActionRow(
                    generateSource(formatted),
                    editCodeButton(event.messageIdLong),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(event.jda, event.messageIdLong, raw, bindings)
            return
        }

        val responseString = response.toString()

        if (responseString.length > 1993) {
            message.reply("⚠ Result was too long. Please get it from the Hastebin!")
                .setActionRow(
                    generateSource(formatted),
                    generateResult(responseString),
                    editCodeButton(event.messageIdLong),
                    deleteMessageButton()
                )
                .mentionRepliedUser(false)
                .queue()
            waitForEdit(event.jda, event.messageIdLong, raw, bindings)
        } else {
            message.reply("```\n${response}```")
                .setActionRow(
                    generateSource(formatted),
                    generateResult(responseString),
                    editCodeButton(event.messageIdLong),
                    deleteMessageButton()
                )
                .mentionRepliedUser(false)
                .await()
            waitForEdit(event.jda, event.messageIdLong, raw, bindings)
        }
    }

    private suspend fun waitForEdit(jda: JDA, messageId: Long, content: String, bindings: Map<String, Any>) {
        val buttonEvent = jda.await<ButtonInteractionEvent>(5.minutes) {
            if (it.user.idLong !in Bean.WHITELISTED_USERS) {
                it.reply("This maze isn't meant for you!").setEphemeral(true).queue()
                return@await false
            }

            it.componentId == "eval-$messageId"
        } ?: return

        val textInput = TextInput.create("eval", "Code", TextInputStyle.PARAGRAPH)
            .setValue(content)
            .build()

        buttonEvent.replyModal(Modal.create("eval-$messageId", "Eval").addActionRow(textInput).build()).queue()
        val modalEvent = jda.await<ModalInteractionEvent>(5.minutes) { it.modalId == "eval-$messageId" } ?: return

        val input = modalEvent.getValue("eval")!!.asString

        val formatted = try {
            parse(input, defaultImports)
        } catch (ex: ParseError) {
            val error = ex.toString()
            modalEvent.reply("An error occurred while formatting the code!" + if (error.length > 1000) "" else "\n```\n$error```")
                .addActionRow(
                    generateError(ex.toString()),
                    generateSource(input),
                    editCodeButton(messageId),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(jda, messageId, input, bindings)
            return
        }

        bindings.forEach { (t, u) -> engine.put(t, u) }

        val response = try {
            (engine.eval(formatted) as Deferred<*>).await()
        } catch (ex: Exception) {
            val error = ex.toString()
            modalEvent.reply("An error occurred while running the Kotlin script!" + if (error.length > 1000) "" else "\n```\n$error```")
                .addActionRow(
                    generateError(ex.toString()),
                    generateSource(formatted),
                    editCodeButton(messageId),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(jda, messageId, input, bindings)
            return
        }

        if (response is Unit) {
            modalEvent.reply("Code executed without errors")
                .addActionRow(
                    generateSource(formatted),
                    editCodeButton(messageId),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(jda, messageId, input, bindings)
            return
        }

        val responseString = response.toString()

        if (responseString.length > 1993) {
            modalEvent.reply("⚠ Result was too long. Please get it from the Hastebin!")
                .addActionRow(
                    generateSource(formatted),
                    generateResult(responseString),
                    editCodeButton(messageId),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(jda, messageId, input, bindings)
        } else {
            modalEvent.reply("```\n${response}```")
                .addActionRow(
                    generateSource(formatted),
                    generateResult(responseString),
                    editCodeButton(messageId),
                    deleteMessageButton()
                )
                .queue()
            waitForEdit(jda, messageId, input, bindings)
        }
    }
}

private fun parse(input: String, imports: List<String>): String {
    val split = if (input.startsWith("```") && input.endsWith("```")) {
        input.substring(input.indexOf("\n"), input.length - 3).split("\n")
    } else {
        input.split("\n")
    }

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

private fun generateError(content: String): Button {
    val link = runCatching { Hastebin.post(content, false) }
        .getOrNull()

    return if (link != null) errorLinkButton(link, "Error") else errorLinkButton(
        "https://nop.link",
        "Error"
    ).asDisabled()
}

private fun generateSource(content: String): Button {
    val link = runCatching { Hastebin.post(content, false, "kt") }
        .getOrNull()

    return if (link != null) sourceLinkButton(link, "Source-Code") else sourceLinkButton(
        "https://nop.link",
        "Source-Code"
    ).asDisabled()
}

private fun generateResult(content: String): Button {
    val link = runCatching { Hastebin.post(content, false) }
        .getOrNull()

    return if (link != null) resultLinkButton(link, "Result") else resultLinkButton(
        "https://nop.link",
        "Result"
    ).asDisabled()
}

private fun resultLinkButton(url: String, label: String) =
    Button.link(url, label).withEmoji(Emoji.fromUnicode("\uD83D\uDEE0"))

private fun deleteMessageButton() = Button.danger("deletemsg", "Delete").withEmoji(Emoji.fromUnicode("\uD83D\uDDD1"))

private fun editCodeButton(messageId: Long) = Button.primary("eval-$messageId", "Edit & Re-run Code")
    .withEmoji(Emoji.fromCustom("repeat", 940204537355063346, false))

private fun errorLinkButton(url: String, label: String) =
    Button.link(url, label).withEmoji(Emoji.fromCustom("error", 943524725487968298, false))

private fun sourceLinkButton(url: String, label: String) =
    Button.link(url, label).withEmoji(Emoji.fromUnicode("\uD83D\uDCDD"))
