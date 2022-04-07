package at.xirado.bean.interaction

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import java.util.*

interface GenericCommand {
    val commandData: CommandData
    val requiredUserPermissions: EnumSet<Permission>
    val requiredBotPermissions: EnumSet<Permission>
    val type: Command.Type
    val global: Boolean
    val enabledGuilds: Set<Long>
    fun setEnabledGuilds(vararg guildIds: Long)
    fun addRequiredUserPermissions(permission: Permission, vararg permissions: Permission)
    fun addRequiredBotPermissions(permission: Permission, vararg permissions: Permission)
    val commandFlags: EnumSet<CommandFlag>
    fun addCommandFlags(vararg commandFlags: CommandFlag)
    fun hasCommandFlag(commandFlag: CommandFlag): Boolean
}