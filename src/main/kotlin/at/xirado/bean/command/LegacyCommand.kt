package at.xirado.bean.command

import at.xirado.bean.Application
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.*

abstract class LegacyCommand(val name: String) : Command {
    abstract val application: Application
    val requiredUserPerms = EnumSet.noneOf(Permission::class.java)
    val requiredBotPerms = EnumSet.noneOf(Permission::class.java)
    var devOnly = false

    abstract suspend fun execute(event: MessageReceivedEvent, args: Arguments)

}