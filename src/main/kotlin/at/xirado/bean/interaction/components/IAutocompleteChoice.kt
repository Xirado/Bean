package at.xirado.bean.interaction.components

import net.dv8tion.jda.api.interactions.commands.Command.Choice

interface IAutocompleteChoice {
    fun toChoice(): Choice
}