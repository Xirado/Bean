package at.xirado.bean.command.commands

import at.xirado.bean.Bean
import at.xirado.bean.command.Command
import at.xirado.bean.command.CommandContext
import at.xirado.bean.command.CommandFlag
import at.xirado.bean.misc.Hastebin
import com.facebook.ktfmt.format.Formatter
import kotlinx.coroutines.Deferred
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import javax.script.ScriptEngineManager

private val errorEmote = "<:error:943524725487968298>"

private val defaultImports = listOf(
        "kotlinx.coroutines.async",
        "dev.minn.jda.ktx.await",
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
        val raw = context.arguments.rawArguments
        if (raw == null) {
            message.reply("Error: missing arguments!").mentionRepliedUser(false).queue()
            return
        }

        val input = if (raw.startsWith("```") && raw.endsWith("```")) {
            raw.substring(raw.indexOf("\n"), raw.length - 3).split("\n")
        } else {
            raw.split("\n")
        }

        val imports = defaultImports.toMutableList()
        val toEval = mutableListOf<String>()

        input.forEach {
            if (it.startsWith("import ")) {
                val import = it.substring(7)
                imports.add(import)
                return@forEach
            }
            toEval.add(it)
        }

        val sb = StringBuilder()

        imports.forEach { sb.append("import $it\n") }

        sb.append("\n")

        sb.append("scope.async {\n")
        toEval.filter { it.isNotBlank() }.forEach { sb.append("$it\n") }
        sb.append("}")

        engine.put("scope", Bean.getInstance().commandHandler.scope)
        engine.put("channel", event.channel)
        engine.put("guild", event.guild)
        engine.put("jda", event.jda)
        engine.put("user", event.author)
        engine.put("author", event.author)
        engine.put("member", event.member)
        engine.put("api", event.jda)
        engine.put("bot", event.jda.selfUser)
        engine.put("selfUser", event.jda.selfUser)
        engine.put("selfMember", event.guild.selfMember)
        engine.put("log", LoggerFactory.getLogger(Bean::class.java) as Logger)

        val unformatted = sb.toString()

        val formatted = try {
            Formatter.format(unformatted, true)
        } catch (ex: Exception) {
            message.reply("An error occurred while formatting the code!")
                    .mentionRepliedUser(false)
                    .setActionRow(errorLinkButton(Hastebin.post(ex.toString(), false), "Error"), sourceLinkButton(Hastebin.post(unformatted, false, "kt"), "Source-Code"))
                    .queue()
            return
        }

        val response = try {
            (engine.eval(formatted) as Deferred<*>).await()
        } catch (ex: Exception) {
            message.reply("An error occurred while running script!")
                    .mentionRepliedUser(false)
                    .setActionRow(errorLinkButton(Hastebin.post(ex.toString(), false), "Error"), sourceLinkButton(Hastebin.post(formatted, false, "kt"), "Source-Code"))
                    .queue()
            return
        }

        if (response is Unit) {
            kotlin.runCatching {
                message.reply("Code executed without errors")
                        .mentionRepliedUser(false)
                        .setActionRow(sourceLinkButton(Hastebin.post(formatted, false, "kt"), "Source-Code"))
                        .queue()
            }
            return
        }

        val responseString = response.toString()

        if (responseString.length > 1993) {
            event.channel.sendFile(responseString.toByteArray(StandardCharsets.UTF_8), "result.txt")
                    .setActionRow(sourceLinkButton(Hastebin.post(formatted, false, "kt"), "Source-Code"))
                    .reference(message).mentionRepliedUser(false).queue()
        } else {
            message.reply("```\n$response```")
                    .setActionRow(sourceLinkButton(Hastebin.post(formatted, false, "kt"), "Source-Code"))
                    .mentionRepliedUser(false)
                    .queue()
        }
    }
}

private fun errorLinkButton(url: String, label: String) = Button.link(url, label).withEmoji(Emoji.fromEmote("error", 943524725487968298, false))

private fun sourceLinkButton(url: String, label: String) = Button.link(url, label).withEmoji(Emoji.fromMarkdown("\uD83D\uDCDD"))
