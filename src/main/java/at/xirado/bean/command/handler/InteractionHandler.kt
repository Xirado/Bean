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
import at.xirado.bean.command.*
import at.xirado.bean.misc.EmbedUtil
import at.xirado.bean.misc.Metrics
import at.xirado.bean.misc.Util
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.internal.utils.Checks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors


private val ERROR_EMBED = EmbedUtil.errorEmbed("An unknown error occurred. Please try again later.")

class InteractionHandler(val bean: Bean) {

    companion object {
        val AUTOCOMPLETE_MAX_CHOICES = 25

        private val log = LoggerFactory.getLogger(InteractionHandler::class.java) as Logger

        private const val commandsPackage = "at.xirado.bean.command.slashcommands"
    }

    private val debugGuilds = bean.config.debugGuilds;
    private val globalCommands: MutableList<GenericCommand> = Collections.synchronizedList(mutableListOf())
    private val guildCommands: MutableMap<Long, MutableList<GenericCommand>> = ConcurrentHashMap()

    fun init() {
        registerCommands()
    }

    fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val guild = event.guild!!
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id) ?: return

        val missingPerms = getMissingPermissions(event.member!!, event.guildChannel, command.requiredUserPermissions)
        if (missingPerms.isNotEmpty())
            return

        command::class.java.methods.forEach { method ->
            if (!method.isAnnotationPresent(AutoComplete::class.java))
                return@forEach

            if (method.parameterCount != 1 || CommandAutoCompleteInteractionEvent::class.java !in method.parameterTypes)
                return@forEach

            val annotation = method.getAnnotation(AutoComplete::class.java) as AutoComplete
            if (annotation.optionName != event.focusedOption.name)
                return@forEach

            Bean.getInstance().commandExecutor.execute { method.invoke(command, event) }
            return
        }
    }

    fun handleCommand(event: GenericCommandInteractionEvent) {
        val guild = event.guild ?: return
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id) ?: return
        val member = event.member!!

        if (event.channel == null) {
            event.reply("Cannot run commands in this channel.").queue()
            log.warn("Received command event from null channel")
            return
        }

        val missingUserPerms = getMissingPermissions(member, event.guildChannel, command.requiredUserPermissions)

        if (!missingUserPerms.isEmpty()) {
            val singular = missingUserPerms.size == 1
            val parsed = missingUserPerms.stream().map { "`${it}`" }.collect(Collectors.joining(", "))
            event.replyEmbeds(EmbedUtil.noEntryEmbed("You are missing the following ${if (singular) "permission" else "permissions"}:\n$parsed"))
                .setEphemeral(true).queue()
            log.debug("Refusing execution of command ${command.commandData.name} (Type=${command.type}, Guild=${guild.idLong}, User=${member.idLong}, Channel=${event.guildChannel.idLong}): User Missing permissions $missingUserPerms")
            return
        }

        val missingBotPerms =
            getMissingPermissions(guild.selfMember, event.guildChannel, command.requiredBotPermissions)

        if (!missingBotPerms.isEmpty()) {
            val singular = missingBotPerms.size == 1
            val parsed = missingBotPerms.stream().map { "`${it}`" }.collect(Collectors.joining(", "))
            event.replyEmbeds(EmbedUtil.noEntryEmbed("I am missing the following ${if (singular) "permission" else "permissions"}:\n$parsed"))
                .setEphemeral(true).queue()
            log.debug("Refusing execution of command ${command.commandData.name} (Type=${command.type}, Guild=${guild.idLong}, User=${member.idLong}, Channel=${event.guildChannel.idLong}): Bot Missing permissions $missingBotPerms")
            return
        }

        val context = SlashCommandContext(event)

        if (CommandFlag.MUST_BE_IN_VC in command.commandFlags) {
            val voiceState = member.voiceState!!
            if (voiceState.channel == null) {
                event.replyEmbeds(EmbedUtil.noEntryEmbed("You must be listening in a voice-channel to use this command!"))
                    .setEphemeral(true).queue()
                return
            }
        }

        if (CommandFlag.MUST_BE_IN_SAME_VC in command.commandFlags) {
            val userVoiceState = member.voiceState!!
            val botVoiceState = guild.selfMember.voiceState!!
            if (botVoiceState.channel != null) {
                if (userVoiceState.channel != botVoiceState.channel) {
                    event.replyEmbeds(EmbedUtil.noEntryEmbed("You must be listening in ${botVoiceState.channel!!.asMention} to use this command!"))
                        .queue()
                    return
                }
            }
        }

        Bean.getInstance().commandExecutor.submit {
            try {
                when (command) {
                    is SlashCommand -> {
                        val slashEvent = event as SlashCommandInteractionEvent
                        command.executeCommand(slashEvent, context)
                        Metrics.COMMANDS.labels("success", command.commandData.name).inc()
                    }
                }
            } catch (ex: Exception) {
                handleError(event, ex)
            }
        }
    }

    private fun handleError(event: GenericCommandInteractionEvent, exception: Exception) {
        Metrics.COMMANDS.labels("failed", event.name).inc()
        log.error("An unhandled exception in a command occurred", exception)
        if (event.isAcknowledged) {
            event.hook.sendMessageEmbeds(ERROR_EMBED)
                .addActionRow(Util.getSupportButton())
                .queue()
        } else {
            event.replyEmbeds(ERROR_EMBED)
                .addActionRow(Util.getSupportButton())
                .setEphemeral(true).queue()
        }

        val path = StringBuilder("/${event.fullCommandName}")
        for (option in event.options) {
            path.append(" *${option.name}* : `${option.asString}`")
        }
        val builder = EmbedBuilder()
            .setTitle("An error occurred while executing a slash-command!")
            .addField(
                "Guild",
                if (event.guild == null) "None (Direct message)" else "${event.guild!!.idLong} (${event.guild!!.name})",
                true
            )
            .addField("Channel", if (event.guild == null) "None (Direct message)" else event.channel!!.name, true)
            .addField("User", event.user.asMention + " (" + event.user.asTag + ")", true)
            .addField("Command", path.toString(), false)
            .setColor(EmbedUtil.ERROR_COLOR)
        event.jda.openPrivateChannelById(Bean.OWNER_ID)
            .flatMap {
                it.sendMessageEmbeds(builder.build()).setContent("```fix\n${exception.stackTraceToString()}```")
            }
            .queue()
    }

    private fun registerCommands() {
        val updateAction = bean.shardManager.shards[0].updateCommands()

        registerCommandsOfClass<SlashCommand>(updateAction)

        updateAction.queue()
    }

    private fun registerCommand(action: CommandListUpdateAction, command: GenericCommand) {
        Checks.notNull(command, "Command")

        if (command.isGlobal) {
            if (bean.isDebug) {
                debugGuilds?.let {
                    it.forEach { addGuildCommand(it, command) }
                }
                return
            }

            globalCommands.add(command)
            action.addCommands(command.commandData)
            return
        }

        val enabledGuilds = command.enabledGuilds
        enabledGuilds.forEach { addGuildCommand(it, command) }
    }

    private fun addGuildCommand(guildId: Long, command: GenericCommand) {
        val enabledCommands = if (guildCommands.containsKey(guildId)) guildCommands[guildId] else mutableListOf()

        if (command in enabledCommands!!)
            throw IllegalArgumentException("${command.type} ${command.commandData.name} has already been registered!")

        enabledCommands.add(command)

        guildCommands[guildId] = enabledCommands
    }

    /**
     * Called by the JDA GuildReadyEvent
     */
    fun updateGuildCommands(guild: Guild) {
        val guildId = guild.idLong
        if (guildId !in guildCommands || guildCommands[guildId]!!.isEmpty())
            return
        val updateAction = guild.updateCommands()
        val commands = guildCommands[guildId]!!
        commands.forEach { updateAction.addCommands(it.commandData) }
        updateAction.queue {
            it.forEach { command ->
                log.debug("Registered command ${command.name} on guild $guildId")
            }
        }
    }

    private inline fun <reified T> registerCommandsOfClass(action: CommandListUpdateAction) {
        val scanResult = ClassGraph().acceptPackages(commandsPackage).enableClassInfo().scan()

        scanResult.getSubclasses(T::class.java).loadClasses().forEach {
            try {
                registerCommand(action, it.getDeclaredConstructor().newInstance() as GenericCommand)
                log.debug("Found interaction-command ${it.name}")
            } catch (e: Exception) {
                log.error("An error occurred while registering a command", e)
            }
        }
        scanResult.close()
    }

    fun getGuildCommands(): Map<Long, List<GenericCommand>> {
        return Collections.unmodifiableMap(guildCommands)
    }

    fun getPublicCommands() = globalCommands.toList()

    private fun getGenericCommand(name: String, type: Int): GenericCommand? {
        return globalCommands.stream()
            .filter { it.commandData.name.equals(name, true) && it.type.id == type }
            .findFirst().orElse(null)
    }

    private fun getGenericCommand(guildId: Long, name: String, type: Int): GenericCommand? {
        return guildCommands.getOrDefault(guildId, mutableListOf())
            .stream()
            .filter { it.commandData.name.equals(name, true) && it.type.id == type }
            .findFirst()
            .orElse(getGenericCommand(name, type))
    }

    private fun getMissingPermissions(
        member: Member,
        channel: GuildChannel,
        requiredPerms: EnumSet<Permission>
    ): Collection<Permission> {
        val memberPerms = member.getPermissions(channel)

        return requiredPerms.filter { it !in memberPerms }
    }
}
