package at.xirado.bean.listener

import at.xirado.bean.Application
import dev.minn.jda.ktx.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.RestAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.script.ScriptEngineManager

class ModalListener(private val application: Application): ListenerAdapter() {

    companion object {
        private val SCRIPT_ENGINE = ScriptEngineManager().getEngineByName("groovy")
        private val DEFAULT_IMPORTS = listOf(
            "net.dv8tion.jda.api.entities.impl",
            "net.dv8tion.jda.api.managers",
            "net.dv8tion.jda.api.entities",
            "net.dv8tion.jda.api",
            "net.dv8tion.jda.api.utils",
            "net.dv8tion.jda.api.utils.data",
            "net.dv8tion.jda.internal.requests",
            "net.dv8tion.jda.api.requests",
            "java.lang",
            "java.io",
            "java.math",
            "java.util",
            "java.util.concurrent",
            "java.time"
        )
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        application.coroutineScope.launch {
            val userId = event.user.idLong
            if (event.modalId == "eval") {
                if (userId !in application.config.devUsers) {
                    event.reply("You are not allowed to use this!").setEphemeral(true).queue()
                    return@launch
                }
                event.deferReply(true).queue()
                SCRIPT_ENGINE.put("guild", event.guild)
                SCRIPT_ENGINE.put("author", event.user)
                SCRIPT_ENGINE.put("member", event.member)
                SCRIPT_ENGINE.put("channel", event.channel)
                SCRIPT_ENGINE.put("jda", event.jda)
                SCRIPT_ENGINE.put("api", event.jda)
                SCRIPT_ENGINE.put("bot", event.jda.selfUser)
                SCRIPT_ENGINE.put("selfuser", event.jda.selfUser)
                SCRIPT_ENGINE.put("selfmember", event.guild!!.selfMember)
                SCRIPT_ENGINE.put("log", LoggerFactory.getLogger(Application::class.java) as Logger)

                val toEval = StringBuilder()

                DEFAULT_IMPORTS.forEach { toEval.append("import ${it}.*;\n") }

                toEval.append(event.getValue("eval")!!.asString)

                try {
                    val evaluated = SCRIPT_ENGINE.eval(toEval.toString())

                    if (evaluated is RestAction<*>) {
                        val result = evaluated.await()
                        event.hook.sendMessage("Got `${result.javaClass.simpleName}`: `$result`").queue()
                        return@launch
                    }
                    if (evaluated == null) {
                        event.hook.sendMessage("Executed without errors!").queue()
                        return@launch
                    }

                    event.hook.sendMessage("Got `${evaluated.javaClass.simpleName}`: `$evaluated`").queue()
                } catch (ex: Exception) {
                    val formatted = "Error:\n```\n${ex.message}\n```"
                    event.hook.sendMessage(formatted).queue()
                }
            }
        }
    }
}