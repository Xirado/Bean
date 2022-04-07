package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.io.config.ConfigLoader
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class InfoCommand(private val application: Application): SlashCommand("info", "shows some info about this bot") {

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val lang = ConfigLoader.loadResourceAsYaml("i18n/de.yml")

        event.reply(lang.getObject("general").getString("not_found")).await()
    }
}