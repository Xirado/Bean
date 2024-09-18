package at.xirado.bean.interaction.command.model.slash.dsl

import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.util.createOption
import net.dv8tion.jda.api.interactions.commands.build.OptionData

inline fun <reified T> SlashCommand.option(
    name: String,
    description: String,
    autocomplete: Boolean = false,
    builder: OptionData.() -> Unit = {}
) {
    val option = createOption<T>(name, description, autocomplete, builder)
    commandData.addOptions(option)
}