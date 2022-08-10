package at.xirado.bean.interaction

import at.xirado.bean.Application
import at.xirado.bean.executor
import at.xirado.bean.util.*
import dev.minn.jda.ktx.await
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.internal.utils.Checks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

private val log = LoggerFactory.getLogger(InteractionCommandHandler::class.java) as Logger

private const val commandsPackage = "at.xirado.bean.interaction.command"

class InteractionCommandHandler(private val application: Application) {
    private val globalCommands: MutableList<GenericCommand> = Collections.synchronizedList(mutableListOf())
    private val guildCommands: MutableMap<Long, MutableList<GenericCommand>> = ConcurrentHashMap()

    fun init() {
        registerCommands()
    }

    suspend fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val guild = event.guild!!
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id)?: return

        if (getMissingPermissions(event.member!!, event.guildChannel, command.requiredUserPermissions).isNotEmpty())
            return

        val method = command::class.members
            .filter { it.hasAnnotation<AutoComplete>() }
            .firstOrNull { it.findAnnotation<AutoComplete>()?.option == event.focusedOption.name }

        method?.callSuspend(command, event)
    }

    suspend fun handleCommand(event: GenericCommandInteractionEvent) {
        val guild = event.guild!!
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id)?: return
        val member = event.member!!

        val missingUserPerms = getMissingPermissions(member, event.guildChannel, command.requiredUserPermissions)

        if (missingUserPerms.isNotEmpty()) {
            val singular = missingUserPerms.size == 1
            val parsed = missingUserPerms.stream().map { "`${it}`" }.collect(Collectors.joining(", "))
            event.reply(":x: You are missing the following ${if (singular) "permission" else "permissions"}:\n$parsed").await()
            log.debug("Refusing execution of command ${command.commandData.name} (Type=${command.type}, Guild=${guild.idLong}, User=${member.idLong}, Channel=${event.guildChannel.idLong}): User Missing permissions $missingUserPerms")
            return
        }

        val missingBotPerms = getMissingPermissions(guild.selfMember, event.guildChannel, command.requiredBotPermissions)

        if (missingBotPerms.isNotEmpty()) {
            val singular = missingBotPerms.size == 1
            val parsed = missingBotPerms.stream().map { "`${it}`" }.collect(Collectors.joining(", "))
            event.reply(":x: I am missing the following ${if (singular) "permission" else "permissions"}:\n$parsed").await()
            log.debug("Refusing execution of command ${command.commandData.name} (Type=${command.type}, Guild=${guild.idLong}, User=${member.idLong}, Channel=${event.guildChannel.idLong}): Bot Missing permissions $missingBotPerms")
            return
        }

        if (!command.commandFlags.all { it.filter.invoke(event) })
            return

        if (event.subcommandName != null) {
            val method = command::class.members
                .filter { it.hasAnnotation<SubCommand>() }
                .firstOrNull { it.findAnnotation<SubCommand>()?.name == event.subcommandName }

            runCatching { method?.callSuspend(command, event) }.onFailure { handleError(command, event, it) }
            return
        }

        when (command) {
            is SlashCommand -> {
                val slashEvent = event as SlashCommandInteractionEvent
                runCatching { command.baseCommand(slashEvent) }.onFailure { handleError(command, event, it) }
            }
        }
    }

    private fun handleError(command: GenericCommand, event: GenericCommandInteractionEvent, throwable: Throwable) {
        log.error("An unhandled error was encountered", throwable)
        val locale = event.getUserI18n()

        val errorMessage = locale.get("general.unknown_error_occurred")
        val supportMessage = locale.get("general.support")
        val button = SUPPORT_BUTTON.withLabel(supportMessage)

        val stackTrace = if (event.user.idLong in application.config.devUsers) parseThrowable(throwable) else null
        val date = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("YYYY-MM-dd--HH-mm-ss")
        val formattedDate = date.format(format)

        val fileName = "stacktrace_${event.name}_$formattedDate.ansi"
        if (event.isAcknowledged)
            event.sendErrorMessage(errorMessage, ephemeral = true).addActionRow(button).apply {
                if (stackTrace != null)
                    addFile(stackTrace.toByteArray(), fileName)
            }.queue()
        else
            event.replyError(errorMessage, ephemeral = true).addActionRow(button).apply {
                if (stackTrace != null)
                    addFile(stackTrace.toByteArray(), fileName)
            }.queue()
    }

    private fun parseThrowable(throwable: Throwable): String {
        return buildAnsi {
            reset()
            fg(AnsiForegroundColor.RED)
            append(throwable.stackTraceToString())
            reset()
        }
    }

    private fun registerCommands() {
        val updateAction = application.shardManager.shards[0].updateCommands()

        registerCommandsOfClass(SlashCommand::class.java, updateAction)

        updateAction.queue()
    }

    private fun registerCommand(action: CommandListUpdateAction, command: GenericCommand) {
        val config = application.config
        Checks.notNull(command, "Command")
        if (command.requiredUserPermissions.isNotEmpty()) {
            command.commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(command.requiredUserPermissions)
        }

        if (command.global && !config.devMode) {
            globalCommands.add(command)
            action.addCommands(command.commandData)
            executor.execute { application.listenerManager.registerListeners(command) }
            return
        }

        val enabledGuilds = if (config.devMode) config.devGuilds else command.enabledGuilds
        enabledGuilds.forEach { addGuildCommand(it, command) }
        executor.execute { application.listenerManager.registerListeners(command) }
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
        commands.forEach { updateAction.addCommands(it.commandData)}
        updateAction.queue {
            it.forEach {  command ->
                log.debug("Registered command ${command.name} on guild $guildId")
            }
        }
    }

    private fun registerCommandsOfClass(clazz: Class<out GenericCommand>, action: CommandListUpdateAction) {
        ClassGraph().acceptPackages(commandsPackage).enableClassInfo().scan().use {
            it.getSubclasses(clazz).loadClasses().forEach {
                runCatching {
                    log.debug("Found interaction-command ${it.name}")
                    registerCommand(
                        action,
                        it.getDeclaredConstructor(Application::class.java).newInstance(application) as GenericCommand
                    )
                }.onFailure { ex -> log.error("An error occurred while loading an application-command", ex) }
            }
        }
    }

    fun getGuildCommands() : Map<Long, List<GenericCommand>> {
        return Collections.unmodifiableMap(guildCommands)
    }

    private fun getGenericCommand(name: String, type: Int) : GenericCommand? {
        return globalCommands.stream()
            .filter { it.commandData.name == name && it.type.id == type}
            .findFirst().orElse(null)
    }

    private fun getGenericCommand(guildId: Long, name: String, type: Int) : GenericCommand? {
        return guildCommands.getOrDefault(guildId, listOf())
            .stream()
            .filter { it.commandData.name == name && it.type.id == type }
            .findFirst()
            .orElse(getGenericCommand(name, type))
    }

    private fun getMissingPermissions(member: Member, channel: GuildChannel, requiredPerms: EnumSet<Permission>) : EnumSet<Permission> {
        val perms = EnumSet.noneOf(Permission::class.java)

        for (permission in requiredPerms) {
            if (!member.hasPermission(channel, permission))
                perms.add(permission)
        }
        return perms
    }
}