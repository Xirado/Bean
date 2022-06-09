package at.xirado.bean.interaction.components

import net.dv8tion.jda.api.interactions.commands.Command

private const val HISTORY_INDICATOR = "\uD83D\uDD53"

class SearchHistorySuggestion(val name: String, val value: String, val isPlaylist: Boolean) : IAutocompleteChoice {

    private fun formatName(): String {
        val text = buildString {
            append("$HISTORY_INDICATOR ")
            if (isPlaylist)
                append("$PLAYLIST_INDICATOR ")
            append(name)
        }
        return if (text.length > 100) text.take(97) + "..." else text
    }

    private fun formatValue() = if (value.length > 100) value.take(100) else value

    override fun toChoice() = Command.Choice(formatName(), formatValue())
}