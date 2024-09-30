package at.xirado.bean.interaction.command.model.slash

import at.xirado.bean.interaction.autoDefer
import at.xirado.bean.interaction.command.AppCommandHandler
import at.xirado.bean.interaction.command.model.AppCommand
import at.xirado.bean.model.GuildFeature
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import at.xirado.bean.util.checkCommandFunctionParameters
import at.xirado.bean.util.findFunctionWithAnnotation
import at.xirado.bean.util.getPopulatedCommandParameters
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy

abstract class SlashCommand(
    name: String,
    description: String,
) : AppCommand<SlashCommandInteractionEvent> {
    override val commandData: SlashCommandData = Commands.slash(name, description)
    override val type: Command.Type = Command.Type.SLASH
    override val identifier: String = "slash:$name"
    override val requiredGuildFlags: EnumSet<GuildFlag> = EnumSet.noneOf(GuildFlag::class.java)
    override val requiredUserFlags: EnumSet<UserFlag> = EnumSet.noneOf(UserFlag::class.java)
    override var feature: GuildFeature? = null
    private val handler: Pair<Handler, KFunction<*>>? = findFunctionWithAnnotation<Handler>()
    private val subcommands = mutableMapOf<String, Subcommand>()
    private val subcommandGroups = mutableMapOf<String, SubcommandGroup>()

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val subcommandName = event.subcommandName
        val subcommandGroup = event.subcommandGroup

        val (handlerPair, instance) = when {
            subcommandGroup != null -> {
                val group = subcommandGroups[subcommandGroup]
                    ?: throw IllegalStateException("No such group $subcommandGroup")

                val subcommand = group.getSubcommand(subcommandName!!)

                subcommand.handler to subcommand
            }
            subcommandName != null -> {
                val subcommand = subcommands[subcommandName]
                    ?: throw IllegalStateException("No such subcommand $subcommandName")

                subcommand.handler to subcommand
            }
            else -> handler?.to(this)
                ?: throw IllegalStateException("Command has no handler function but no subcommands!")
        }

        val (handler, function) = handlerPair

        val args = getPopulatedCommandParameters(instance, event, function)

        val returnType = function.returnType.classifier as KClass<*>

        if (returnType == MessageCreateData::class) {
            function as KFunction<MessageCreateData>
            event.autoDefer(ephemeral = handler.ephemeral) {
                function.callSuspendBy(args)
            }
        } else {
            function.callSuspendBy(args)
        }
    }

    context(AppCommandHandler) override fun initialize() {
        if (subcommands.isEmpty() && subcommandGroups.isEmpty()) {
            if (handler == null)
                throw IllegalStateException("Command has no handler function")

            checkCommandFunctionParameters(handler.second, commandData.options)
        }

        subcommands.values.forEach { it.initialize() }
        subcommandGroups.values.forEach { it.initialize() }
    }

    fun subcommand(vararg subcommands: Subcommand) {
        commandData.addSubcommands(subcommands.map { it.subcommandData })
        this.subcommands.putAll(
            subcommands.associateBy { it.subcommandData.name }
        )
    }

    fun subcommandGroup(vararg subcommandGroups: SubcommandGroup) {
        commandData.addSubcommandGroups(subcommandGroups.map { it.groupData })
        this.subcommandGroups.putAll(
            subcommandGroups.associateBy { it.groupData.name }
        )
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Handler(val ephemeral: Boolean = false)
