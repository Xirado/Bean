package at.xirado.bean.command.precondition.interaction

import at.xirado.bean.Application
import at.xirado.bean.command.precondition.Precondition
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent

object DeveloperOnlyPrecondition : Precondition<GenericInteractionCreateEvent> {
    override fun check(application: Application, obj: GenericInteractionCreateEvent): Boolean {
        val user = obj.user
        val devUsers = application.config.devUsers

        return user.idLong in devUsers
    }
}