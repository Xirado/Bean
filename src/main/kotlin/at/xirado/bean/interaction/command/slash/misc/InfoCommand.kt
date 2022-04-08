package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.io.config.FileLoader
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class InfoCommand(private val application: Application): SlashCommand("info", "shows some info about this bot") {

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val lang = FileLoader.loadResourceAsYaml("i18n/locales/de.yml")

        event.reply(lang.getObject("general").getString("not_found")).await()
    }
}