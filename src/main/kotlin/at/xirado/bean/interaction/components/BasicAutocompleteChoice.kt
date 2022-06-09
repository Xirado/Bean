package at.xirado.bean.interaction.components

import net.dv8tion.jda.api.interactions.commands.Command

class BasicAutocompleteChoice(val name: String, val value: String) : IAutocompleteChoice {

    override fun toChoice(): Command.Choice {
        return Command.Choice(name, value)
    }
}