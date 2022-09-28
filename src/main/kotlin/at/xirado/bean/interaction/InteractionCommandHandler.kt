package at.xirado.bean.interaction

import at.xirado.bean.Application
import at.xirado.bean.executor
import at.xirado.bean.i18n.LocalizedMessageReference
import at.xirado.bean.interaction.slash.AutoComplete
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.interaction.slash.SubCommand
import at.xirado.bean.io.exception.CommandException
import at.xirado.bean.util.*
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.getOption
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.internal.utils.Checks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod

private val log = LoggerFactory.getLogger(InteractionCommandHandler::class.java) as Logger
private const val commandsPackage = "at.xirado.bean.interaction.command"
private val commandClasses = listOf<Class<out DiscordInteractionCommand>>(SlashCommand::class.java)

private val errorMessage = LocalizedMessageReference("general.unknown_error_occurred")

class InteractionCommandHandler(private val application: Application) {
    private val globalCommands: MutableList<DiscordInteractionCommand> = Collections.synchronizedList(mutableListOf())
    private val guildCommands: MutableMap<Long, MutableList<DiscordInteractionCommand>> = ConcurrentHashMap()

    fun init() {
        registerCommands()
    }

    suspend fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val guild = event.guild ?: return
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id) ?: return

        if (getMissingPermissions(event.member!!, event.guildChannel, command.requiredUserPermissions).isNotEmpty())
            return

        val method = command::class.members
            .filter { it.hasAnnotation<AutoComplete>() }
            .firstOrNull { it.findAnnotation<AutoComplete>()?.option == event.focusedOption.name }

        method?.callSuspend(command, event)
    }

    suspend fun handleCommand(event: GenericCommandInteractionEvent) {
        val guild = event.guild ?: return
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id) ?: return
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

        if (event.subcommandName != null) {
            val method = command::class.members
                .filter { it.hasAnnotation<SubCommand>() }
                .firstOrNull { it.findAnnotation<SubCommand>()?.name == event.subcommandName }

            runCatching { method?.callSuspend(command, event) }.onFailure { handleError(command, event, it.cause ?: it) }
            return
        }

        when (command) {
            is SlashCommand -> {
                event as SlashCommandInteractionEvent
                runCatching { handleSlashCommandReflective(command, event) }
                    .onFailure { handleError(command, event, it.cause ?: it) }
            }
        }
    }

    private suspend fun handleSlashCommandReflective(command: SlashCommand, event: SlashCommandInteractionEvent) {
        if (event.subcommandName == null) {
            val function = command.baseCommandFunction ?: return

            val eventParameter = function.valueParameters.first()
            val map = mutableMapOf<KParameter, Any?>()
            map[function.instanceParameter!!] = command
            map[eventParameter] = event
            function.valueParameters
                .filter { it.name != null && it != eventParameter }
                .forEach { param ->
                    val value = getParameter(param.name!!.snakeCase(), event, param.type)
                    if (param.isOptional && value == null)
                        return@forEach

                    map[param] = value
                }
            //map.forEach { (t, u) -> println("${t.name}: $u") }
            function.callSuspendBy(map)
        }
    }

    private fun getParameter(name: String, event: SlashCommandInteractionEvent, targetType: KType): Any? {
        return when (targetType.classifier) {
            String::class -> event.getOption<String>(name)
            Boolean::class -> event.getOption<Boolean>(name)
            Int::class -> event.getOption<Int>(name)
            Double::class -> event.getOption<Double>(name)
            Long::class -> event.getOption<Long>(name)
            IMentionable::class -> event.getOption<IMentionable>(name)
            User::class -> event.getOption<User>(name)
            Member::class -> event.getOption<Member>(name)
            Message.Attachment::class -> event.getOption<Message.Attachment>(name)
            GuildChannel::class -> event.getOption<GuildChannel>(name)
            Role::class -> event.getOption<Role>(name)
            else -> throw IllegalStateException("Invalid parameter of type ${targetType.classifier}")
        }
    }

    private suspend fun handleCommandException(command: DiscordInteractionCommand, event: GenericCommandInteractionEvent, exception: CommandException) {
        val locale = with(application) { event.getUserI18n() }

        with (command) {
            event.send(exception.responseType, locale.getOrDefault(exception.key), ephemeral = true) {
                if (exception.supportButton) {
                    val supportMessage = locale.getOrDefault("general.support")
                    addActionRow(SUPPORT_BUTTON.withLabel(supportMessage))
                }
            }
        }
    }

    private suspend fun handleError(command: DiscordInteractionCommand, event: GenericCommandInteractionEvent, throwable: Throwable) {
        if (throwable is CommandException)
            return handleCommandException(command, event, throwable)

        log.error("An unhandled exception was encountered", throwable)
        val locale = with(application) { event.getUserI18n() }

        val supportMessage = locale.getValue("general.support")
        val button = SUPPORT_BUTTON.withLabel(supportMessage)

        val stackTrace = if (event.user.idLong in application.config.devUsers) parseThrowable(throwable) else null
        val date = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("YYYY-MM-dd--HH-mm-ss")
        val formattedDate = date.format(format)

        val fileName = "stacktrace_${event.name}_$formattedDate.ansi"
        with (command) {
            event.send(ResponseType.ERROR, errorMessage, ephemeral = true) {
                setActionRow(button)
                if (stackTrace != null)
                    setFiles(FileUpload.fromData(stackTrace.toByteArray(), fileName))
            }
        }
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

    private val commandTypes = mapOf(
        Command.Type.SLASH to "slash",
        Command.Type.MESSAGE to "message",
        Command.Type.USER to "user"
    )

    private fun registerLocalizations(command: DiscordInteractionCommand) {
        val commandData = command.commandData
        val type = commandTypes[commandData.type] ?: return
        val prefix = "interaction.$type.${commandData.name}"

        commandData.setNameLocalizations(
            application.localizationManager.getDiscordLocalizations("$prefix.name")
        )

        if (commandData is SlashCommandData) {
            commandData.setDescriptionLocalizations(
                application.localizationManager.getDiscordLocalizations("$prefix.description")
            )
        }
    }

    private fun registerCommand(action: CommandListUpdateAction, command: DiscordInteractionCommand) {
        val config = application.config
        Checks.notNull(command, "Command")

        if (command.disabled)
            return

        if (command.requiredUserPermissions.isNotEmpty()) {
            command.commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(command.requiredUserPermissions)
        }

        if (command::class.memberFunctions.find { it.name == "onEvent" }?.javaMethod?.declaringClass !in commandClasses)
            application.shardManager.addEventListener(command)

        if (command is SlashCommand) {
            if (command.description.isNotBlank())
                command.commandData.description = command.description
            command.baseCommandFunction = command::class.memberFunctions
                .find { it.hasAnnotation<BaseCommand>() }
        }

        registerLocalizations(command)

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

    private fun addGuildCommand(guildId: Long, command: DiscordInteractionCommand) {
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

    private fun registerCommandsOfClass(clazz: Class<out DiscordInteractionCommand>, action: CommandListUpdateAction) {
        ClassGraph().acceptPackages(commandsPackage).enableClassInfo().scan().use {
            it.getSubclasses(clazz).loadClasses().forEach {
                runCatching {
                    log.debug("Found interaction-command ${it.name}")
                    registerCommand(
                        action,
                        it.getDeclaredConstructor(Application::class.java).newInstance(application) as DiscordInteractionCommand
                    )
                }.onFailure { ex -> log.error("An error occurred while loading an application-command", ex) }
            }
        }
    }

    fun getGuildCommands() : Map<Long, List<DiscordInteractionCommand>> {
        return Collections.unmodifiableMap(guildCommands)
    }

    private fun getGenericCommand(name: String, type: Int) : DiscordInteractionCommand? {
        return globalCommands.stream()
            .filter { it.commandData.name == name && it.type.id == type}
            .findFirst().orElse(null)
    }

    private fun getGenericCommand(guildId: Long, name: String, type: Int) : DiscordInteractionCommand? {
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