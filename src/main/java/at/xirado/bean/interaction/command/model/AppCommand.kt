package at.xirado.bean.interaction.command.model

import at.xirado.bean.database.entity.Guild
import at.xirado.bean.database.entity.User
import at.xirado.bean.interaction.command.AppCommandHandler
import at.xirado.bean.model.GuildFeature
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import at.xirado.bean.util.sha256
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.koin.core.component.KoinComponent
import java.util.*

private val objectMapper = ObjectMapper().apply {
    configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
}

interface AppCommand<E : GenericCommandInteractionEvent> : KoinComponent {
    val commandData: CommandData
    val type: Command.Type
    val identifier: String
    var feature: GuildFeature?
    val requiredGuildFlags: EnumSet<GuildFlag>
    val requiredUserFlags: EnumSet<UserFlag>

    context(AppCommandHandler)
    fun initialize()

    context(AppCommandHandler)
    fun computeHash(): String {
        val commandMap = commandData.toData().toMap()
        val json = objectMapper.writeValueAsString(commandMap)

        return json.sha256()
    }

    suspend fun execute(event: E)
}

fun AppCommand<*>.contexts(vararg types: InteractionContextType) = contexts(types.toList())

fun AppCommand<*>.contexts(types: Collection<InteractionContextType>) {
    commandData.setContexts(types)
}

fun AppCommand<*>.integrationTypes(vararg types: IntegrationType) = integrationTypes(types.toList())

fun AppCommand<*>.integrationTypes(types: Collection<IntegrationType>) {
    commandData.setIntegrationTypes(types)
}

fun AppCommand<*>.isGlobal() = requiredGuildFlags.isEmpty() && feature == null

fun AppCommand<*>.isGuildCommandFor(guild: Guild, defaultFeatures: EnumSet<GuildFeature>): Boolean {
    if (isGlobal()) return false

    val guildFeatures = guild.features ?: defaultFeatures

    feature?.let {
        if (it !in guildFeatures)
            return false
    }

    return requiredGuildFlags.all { it in guild.flags }
}

fun AppCommand<*>.canExecute(user: User): Boolean {
    return requiredUserFlags.all { it in user.flags }
}

fun GenericCommandInteractionEvent.getIdentifier() = when (this) {
    is SlashCommandInteractionEvent -> "slash:$name"
    is MessageContextInteractionEvent -> "message:$name"
    is UserContextInteractionEvent -> "user:$name"
    else -> throw IllegalStateException("Unsupported interaction")
}