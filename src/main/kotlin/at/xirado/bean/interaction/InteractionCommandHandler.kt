package at.xirado.bean.interaction

import at.xirado.bean.Application
import at.xirado.bean.util.replyError
import dev.minn.jda.ktx.await
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
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

class InteractionCommandHandler(private val application: Application) {

    companion object {
        val AUTOCOMPLETE_MAX_CHOICES = 25

        private val log = LoggerFactory.getLogger(InteractionCommandHandler::class.java) as Logger

        private const val commandsPackage = "at.xirado.bean.interaction.command"
    }

    private val globalCommands: MutableList<GenericCommand> = Collections.synchronizedList(mutableListOf())
    private val guildCommands: MutableMap<Long, MutableList<GenericCommand>> = ConcurrentHashMap()

    fun init() {
        registerCommands()
    }

    suspend fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val guild = event.guild!!
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id)?: return

        when (command) {
            is SlashCommand -> {
                command.onAutoComplete(event)
            }
        }
    }

    suspend fun handleCommand(event: GenericCommandInteractionEvent) {
        val guild = event.guild!!
        val guildId = guild.idLong
        val command = getGenericCommand(guildId, event.name, event.commandType.id)?: return
        val member = event.member!!

        if (command.hasCommandFlag(CommandFlag.DEVELOPER_ONLY)) {
            if (member.idLong !in application.config.devUsers) {
                event.replyError("Hold up! You cannot use this :c").setEphemeral(true).await()
                return
            }
        }

        val missingUserPerms = getMissingPermissions(member, event.guildChannel, command.requiredUserPermissions)

        if (!missingUserPerms.isEmpty()) {
            val singular = missingUserPerms.size == 1
            val parsed = missingUserPerms.stream().map { "`${it}`" }.collect(Collectors.joining(", "))
            event.reply(":x: You are missing the following ${if (singular) "permission" else "permissions"}:\n$parsed").await()
            log.debug("Refusing execution of command ${command.commandData.name} (Type=${command.type}, Guild=${guild.idLong}, User=${member.idLong}, Channel=${event.guildChannel.idLong}): User Missing permissions $missingUserPerms")
            return
        }

        val missingBotPerms = getMissingPermissions(guild.selfMember, event.guildChannel, command.requiredBotPermissions)

        if (!missingBotPerms.isEmpty()) {
            val singular = missingBotPerms.size == 1
            val parsed = missingBotPerms.stream().map { "`${it}`" }.collect(Collectors.joining(", "))
            event.reply(":x: I am missing the following ${if (singular) "permission" else "permissions"}:\n$parsed").await()
            log.debug("Refusing execution of command ${command.commandData.name} (Type=${command.type}, Guild=${guild.idLong}, User=${member.idLong}, Channel=${event.guildChannel.idLong}): Bot Missing permissions $missingBotPerms")
            return
        }

        if (CommandFlag.VOICE_CHANNEL_ONLY in command.commandFlags) {
            val voiceState = member.voiceState!!
            if (voiceState.channel == null) {
                event.reply("You must be listening in a voice-channel to use this command!").await()
                return
            }
        }

        if (CommandFlag.SAME_VOICE_CHANNEL_ONLY in command.commandFlags) {
            val userVoiceState = member.voiceState!!
            val botVoiceState = guild.selfMember.voiceState!!
            if (botVoiceState.channel != null) {
                if (userVoiceState.channel != botVoiceState.channel) {
                    event.reply("You must be listening in ${botVoiceState.channel!!.asMention} to use this command!").await()
                    return
                }
            }
        }

        when (command) {
            is SlashCommand -> {
                val slashEvent = event as SlashCommandInteractionEvent
                command.execute(slashEvent)
            }
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

        if (command.global && !config.devMode) {
            globalCommands.add(command)
            action.addCommands(command.commandData)
            return
        }

        val enabledGuilds = if (config.devMode) config.devGuilds else command.enabledGuilds
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
        commands.forEach { updateAction.addCommands(it.commandData)}
        updateAction.queue {
            it.forEach {  command ->
                log.debug("Registered command ${command.name} on guild $guildId")
            }
        }
    }

    private fun registerCommandsOfClass(clazz: Class<out GenericCommand>, action: CommandListUpdateAction) {
        val scanResult = ClassGraph().acceptPackages(commandsPackage).enableClassInfo().scan()

        scanResult.getSubclasses(clazz).loadClasses().forEach {
            try {
                log.debug("Found interaction-command ${it.name}")
                registerCommand(action, it.getDeclaredConstructor(Application::class.java).newInstance(application) as GenericCommand)
            } catch (ex: Exception) {
                log.error("An error occurred while loading an interaction-command", ex)
            }
        }
        scanResult.close()
    }

    fun getGuildCommands() : Map<Long, List<GenericCommand>> {
        return Collections.unmodifiableMap(guildCommands)
    }

    private fun getGenericCommand(name: String, type: Int) : GenericCommand? {
        return globalCommands.stream()
            .filter { it.commandData.name.equals(name, true) && it.type.id == type}
            .findFirst().orElse(null)
    }

    private fun getGenericCommand(guildId: Long, name: String, type: Int) : GenericCommand? {
        return guildCommands.getOrDefault(guildId, listOf())
            .stream()
            .filter { it.commandData.name.equals(name, true) && it.type.id == type }
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