/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.command.handler

import at.xirado.bean.Bean
import at.xirado.bean.command.Command
import at.xirado.bean.command.CommandArgument
import at.xirado.bean.command.CommandContext
import at.xirado.bean.command.CommandFlag
import at.xirado.bean.command.commands.EvalCommand
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

        if (!event.message.contentRaw.startsWith("<@" + event.jda.selfUser.idLong + "> "))
            return

        try {
            val arguments = CommandArgument(event.message.contentRaw, event.jda.selfUser.idLong)

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
