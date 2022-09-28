package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class FollowupTest(override val app: Application) : SlashCommand("followup") {
    init {
        devCommand()
    }

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent) {
        event.reply("Initial response").setEphemeral(true).await()
        event.hook.sendMessage("First followup").setEphemeral(true).await()
        event.hook.sendMessage("Second followup").await()
    }
}