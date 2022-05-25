package at.xirado.bean.data

import net.dv8tion.jda.api.interactions.commands.Command

class Bookmark(
    override val name: String,
    value: String,
    val isPlaylist: Boolean
) : IAutoCompleteChoice {
    override val value: String = value.take(100)

    fun format(): String {
        val text = buildString {
            append(EMOJI)
            if (isPlaylist) append(SCROLL)
            append(" $name")
        }
        return if (text.length > 100) text.take(97) + "..." else text
    }

    override fun toChoice() = Command.Choice(format(), value)

    companion object {
        const val EMOJI = "\uD83D\uDCCC"
        const val SCROLL = "\uD83D\uDCDC" // indicates that this is a playlist
    }
}