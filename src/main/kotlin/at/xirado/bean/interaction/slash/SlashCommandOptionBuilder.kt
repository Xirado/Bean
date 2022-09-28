package at.xirado.bean.interaction.slash

import at.xirado.bean.Application
import dev.minn.jda.ktx.interactions.commands.optionType
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class SlashCommandOptionBuilder(val commandData: SlashCommandData, val application: Application) {
    inline fun <reified T> option(name: String, description: String, required: Boolean = false, autocomplete: Boolean = false, builder: OptionData.() -> Unit = {}) {
        val type = optionType<T>()
        if (type == OptionType.UNKNOWN)
            throw IllegalArgumentException("Cannot resolve type " + T::class.java.simpleName + " to OptionType!")

        val nameLocalizations = application.localizationManager.getDiscordLocalizations("interaction.slash.${commandData.name}.options.$name.name")
        val descriptionLocalizations = application.localizationManager.getDiscordLocalizations("interaction.slash.${commandData.name}.options.$name.description")

        commandData.addOptions(
            OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autocomplete)
                .setNameLocalizations(nameLocalizations)
                .setDescriptionLocalizations(descriptionLocalizations)
                .apply(builder)
        )
    }
}