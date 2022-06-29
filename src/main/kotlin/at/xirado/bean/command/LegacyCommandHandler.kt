package at.xirado.bean.command

import at.xirado.bean.Application
import at.xirado.bean.command.legacy.EvalCommand
import at.xirado.bean.executor
import at.xirado.bean.util.getLog
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

private val log = getLog<LegacyCommandHandler>()

class LegacyCommandHandler(val application: Application) {
    companion object {
        val MENTION_REGEX = "<@!?(\\d+)>\\s*?(\\w+)\\s*([\\s\\S]+)?".toRegex()
    }

    private val commands = mutableMapOf<String, LegacyCommand>()

    init {
        registerCommand(EvalCommand(application))
    }

    suspend fun handleCommand(event: MessageReceivedEvent) {
        val match = MENTION_REGEX.matchEntire(event.message.contentRaw) ?: return
        if (match.groups[1]!!.value != event.jda.selfUser.id)
            return

        val commandName = match.groups[2]!!.value.lowercase()
        if (commandName !in commands.keys)
            return

        val command = commands[commandName]!!

        if (command.devOnly && event.author.idLong !in application.config.devUsers) {
            event.message.reply("This maze isn't meant for you!").queue()
            return
        }

        val args = Arguments(match.groups[3]?.value ?: "")

        command.execute(event, args)
    }


    private fun registerCommand(command: LegacyCommand) {
        commands[command.name.lowercase()] = command
        executor.execute { application.listenerManager.registerListeners(command) }
    }
}

data class Arguments(val raw: String) {
    val list = raw.split("\\s+")

    fun isEmpty() = list.isEmpty()
}