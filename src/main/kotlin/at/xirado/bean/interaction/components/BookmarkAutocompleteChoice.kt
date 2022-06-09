package at.xirado.bean.interaction.components

import net.dv8tion.jda.api.interactions.commands.Command

private const val BOOKMARK_INDICATOR = "\uD83D\uDCCC"
const val PLAYLIST_INDICATOR = "\uD83D\uDCDC"

class BookmarkAutocompleteChoice(val name: String, val url: String, val isPlaylist: Boolean) : IAutocompleteChoice {

    private fun format(): String {
        val text = buildString {
            append("$BOOKMARK_INDICATOR ")
            if (isPlaylist)
                append("$PLAYLIST_INDICATOR ")
            append(name)
        }
        return if (text.length > 100) text.take(97) + "..." else text
    }

    override fun toChoice() = Command.Choice(format(), url)
}