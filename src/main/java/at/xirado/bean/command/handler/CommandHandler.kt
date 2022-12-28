package at.xirado.bean.command.handler

import at.xirado.bean.Bean
import at.xirado.bean.command.Command
import at.xirado.bean.command.CommandArgument
import at.xirado.bean.command.CommandContext
import at.xirado.bean.command.CommandFlag
import at.xirado.bean.command.commands.EvalCommand
import at.xirado.bean.data.GuildManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentSkipListMap

class CommandHandler {

    val scope = getDefaultScope()

    private val registeredCommands: ConcurrentMap<String, Command> =
        ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    init {
        registerCommand(EvalCommand())
    }

    private fun registerCommand(command: Command) {
        val name = command.name
        if (registeredCommands.containsKey(name)) {
            LOGGER.error(
                "Command \"{}\" could not be registered because a command (or alias) with this name already exists!",
                name
            )
            return
        }
        registeredCommands[name] = command
        val commandAliases = command.getAliases()
        for (alias in commandAliases) {
            if (registeredCommands.containsKey(alias)) LOGGER.error(
                "Alias \"{}\" could not be registered because a command (or alias) with this name already exists!",
                alias
            ) else {
                registeredCommands[alias] = command
            }
        }
    }

    fun getRegisteredCommands(): List<Command> {
        return registeredCommands.values.stream().distinct().toList()
    }

    /**
     * Returns all commands accessible in a guild
     *
     * @param guildID the id of the guild
     * @return immutable list containing the commands
     */
    fun getRegisteredCommands(guildID: Long): List<Command> {
        return registeredCommands
            .values
            .stream().distinct()
            .filter { command: Command -> command.isAvailableIn(guildID) }.toList()
    }

    fun getGuildCommands(guildID: Long): List<Command> {
        return registeredCommands
            .values
            .stream().distinct()
            .filter { command: Command ->
                command.hasCommandFlag(CommandFlag.PRIVATE_COMMAND) && command.getAllowedGuilds().contains(guildID)
            }
            .toList()
    }

    fun handleCommandFromGuild(event: MessageReceivedEvent) {
        if (event.author.idLong !in Bean.WHITELISTED_USERS)
            return

        try {
            val guildData = GuildManager.getGuildData(event.guild)
            val arguments = if (event.message.contentRaw.startsWith("<@" + event.jda.selfUser.idLong + "> "))
                CommandArgument(event.message.contentRaw, event.jda.selfUser.idLong)
            else
                CommandArgument(event.message.contentRaw, guildData.prefix)

            val name = arguments.commandName
            if (!registeredCommands.containsKey(name)) return
            val command = registeredCommands[name]
            val context = CommandContext(event, arguments, command, event.member)
            scope.launch { command!!.executeCommand(event, context) }
        } catch (ex: Exception) {
            LOGGER.error("An error occurred whilst executing command", ex)
            event.message.reply(":x: An error occurred").mentionRepliedUser(false).queue()
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CommandHandler::class.java)
    }
}
