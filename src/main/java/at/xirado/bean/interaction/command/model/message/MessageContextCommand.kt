package at.xirado.bean.interaction.command.model.message

import at.xirado.bean.interaction.command.AppCommandHandler
import at.xirado.bean.interaction.command.model.AppCommand
import at.xirado.bean.model.GuildFeature
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.util.*

abstract class MessageContextCommand(name: String) : AppCommand<MessageContextInteractionEvent> {
    override val commandData = Commands.message(name)
    override val type = Command.Type.MESSAGE
    override val identifier = "message:$name"
    override val requiredGuildFlags: EnumSet<GuildFlag> = EnumSet.noneOf(GuildFlag::class.java)
    override val requiredUserFlags: EnumSet<UserFlag> = EnumSet.noneOf(UserFlag::class.java)
    override var feature: GuildFeature? = null

    context(AppCommandHandler) override fun initialize() {

    }
}