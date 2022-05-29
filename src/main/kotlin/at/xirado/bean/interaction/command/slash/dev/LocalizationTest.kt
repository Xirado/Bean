package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.replyError
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class LocalizationTest(override val application: Application) : SlashCommand("localization", "tests the localization system") {

    init {
        devCommand()

        option<String>(name = "tag", description = "Locale tag", required = true)
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val tag = event.getOption<String>("tag")!!
        val localizationManager = application.localizationManager
        val locale = localizationManager.getForLanguageTag(tag)

        val result = locale.get("general.info", "user" to event.user.asTag, "reason" to "Ehrenmann!")?:
            return event.replyError("Path \"general.info\" was not found!").queue()

        event.reply(result).queue()
    }
}