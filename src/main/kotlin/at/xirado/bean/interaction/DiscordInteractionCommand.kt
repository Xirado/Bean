package at.xirado.bean.interaction

import at.xirado.bean.command.DiscordCommand
import at.xirado.bean.i18n.LocalizedMessageReference
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.getGuildI18n
import at.xirado.bean.util.getUserI18n
import at.xirado.bean.util.plainEmbed
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.util.*

interface DiscordInteractionCommand : DiscordCommand {
    val commandData: CommandData
    val requiredUserPermissions: EnumSet<Permission>
    val requiredBotPermissions: EnumSet<Permission>
    val type: net.dv8tion.jda.api.interactions.commands.Command.Type
    val enabledGuilds: Set<Long>
    val global: Boolean
        get() = enabledGuilds.isEmpty()

    var disabled: Boolean

    fun addUserPermissions(vararg permissions: Permission) {
        requiredUserPermissions.addAll(permissions)
    }

    fun addBotPermissions(vararg permissions: Permission) {
        requiredBotPermissions.addAll(permissions)
    }

    suspend fun GenericCommandInteractionEvent.send(type: ResponseType = ResponseType.PRIMARY,
                                                   message: CharSequence,
                                                   vararg attributes: Pair<String, Any> = emptyArray(),
                                                   ephemeral: Boolean = false, builder: MessageCreateBuilder.() -> Unit = {}) {
        val locale = with (app) { if (ephemeral) getUserI18n() else getGuildI18n() }
        val content = when (message) {
            is LocalizedMessageReference -> message.get(locale, *attributes)
            else -> message.toString()
        }
        val embed = plainEmbed(type, type.reference?.get(locale), content)
        val createData = MessageCreateBuilder()
            .apply(builder)
            .addEmbeds(embed)
            .build()
        if (isAcknowledged)
            hook.sendMessage(createData).setEphemeral(ephemeral).await()
        else
            reply(createData).setEphemeral(ephemeral).await()
    }
}