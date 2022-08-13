package at.xirado.bean.interaction

import at.xirado.bean.Application
import at.xirado.bean.command.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import java.util.*

interface GenericCommand : Command {
    val application: Application
    val commandData: CommandData
    val requiredUserPermissions: EnumSet<Permission>
    val requiredBotPermissions: EnumSet<Permission>
    val type: net.dv8tion.jda.api.interactions.commands.Command.Type
    val enabledGuilds: Set<Long>
    val commandFlags: EnumSet<CommandFlag>
    val global: Boolean
        get() = enabledGuilds.isEmpty()

    var disabled: Boolean

    fun addUserPermissions(vararg permissions: Permission) {
        requiredUserPermissions.addAll(permissions)
    }

    fun addBotPermissions(vararg permissions: Permission) {
        requiredBotPermissions.addAll(permissions)
    }
}