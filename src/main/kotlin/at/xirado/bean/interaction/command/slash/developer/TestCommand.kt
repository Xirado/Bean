package at.xirado.bean.interaction.command.slash.developer

import at.xirado.bean.Application
import at.xirado.bean.i18n.LocalizationManager
import at.xirado.bean.interaction.CommandFlag
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.replyError
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class TestCommand(private val application: Application): SlashCommand("test", "this is a test") {

    init {
        setEnabledGuilds(*application.config.devGuilds.toLongArray())
        addCommandFlags(CommandFlag.DEVELOPER_ONLY)
        option(type = OptionType.STRING, "locale", "the locale to test against", required = true, autoComplete = true)
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val option = event.getOption<String>("locale")!!
        val lang = LocalizationManager.getForLanguageTag(option)?: return kotlin.run {
            event.replyError("Locale $option was not found!").await()
        }

        val user = event.user.asTag
        val reason = "he codes in Kotlin"

        val template = lang.get("general.info")!!
        val filled = lang.get("general.info", "user" to user, "reason" to reason)!!
        event.reply("Locale: $option\nTemplate: $template\nFilled out: $filled").await()
    }

    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        event.replyChoiceStrings(LocalizationManager.LANGUAGES.filter { it.startsWith(event.focusedOption.value, true) } .map { it.replace(".yml", "") }.toList()).await()
    }
}