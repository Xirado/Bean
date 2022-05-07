package at.xirado.bean.event

import at.xirado.bean.Bean
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object EvalListener : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.user.idLong !in Bean.WHITELISTED_USERS)
            return

        if (event.componentId == "deletemsg") {
            event.message.delete().queue(null) { }
        }
    }
}
