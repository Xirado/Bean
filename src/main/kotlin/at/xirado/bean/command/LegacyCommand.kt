package at.xirado.bean.command

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

abstract class LegacyCommand(val name: String) : DiscordCommand {
    var devOnly = true

    abstract suspend fun execute(event: MessageReceivedEvent, args: Arguments)

}