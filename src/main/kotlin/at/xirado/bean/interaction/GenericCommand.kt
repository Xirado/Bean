package at.xirado.bean.interaction

import at.xirado.bean.Application
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import java.util.*

interface GenericCommand {
    val application: Application
    val commandData: CommandData
    val requiredUserPermissions: EnumSet<Permission>
    val requiredBotPermissions: EnumSet<Permission>
    val type: Command.Type
    val enabledGuilds: Set<Long>
    val commandFlags: EnumSet<CommandFlag>
    val global: Boolean
        get() = enabledGuilds.isEmpty()
}