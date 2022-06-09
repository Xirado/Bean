package at.xirado.bean.interaction.components

import net.dv8tion.jda.api.interactions.commands.Command

private const val SUGGESTION_INDICATOR = "\uD83D\uDD0D"

class SearchSuggestion(val name: String) : IAutocompleteChoice {

    private fun formatName(): String {
        val text = buildString {
            append("$SUGGESTION_INDICATOR ")
            append(name)
        }
        return if (text.length > 100) text.take(97) + "..." else text
    }

    private fun formatValue() = if (name.length > 100) name.take(100) else name

    override fun toChoice() = Command.Choice(formatName(), formatValue())
}