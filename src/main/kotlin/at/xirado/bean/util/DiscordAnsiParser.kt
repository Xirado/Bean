package at.xirado.bean.util

inline fun ansi(fg: AnsiForegroundColor? = null,
                bg: AnsiBackgroundColor? = null,
                bold: Boolean = false,
                underline: Boolean = false, text:  () -> String) =
    DiscordAnsiParser.parse(text.invoke(), fg, bg, bold, underline, true)

class DiscordAnsiParser {
    companion object {
        const val ESCAPE_CHAR = '\u001b'
        const val RESET_VAL = 0
        const val RESET = "${ESCAPE_CHAR}[${RESET_VAL}m"

        @JvmStatic
        fun parse(text: String,
                  foreground: AnsiForegroundColor? = null,
                  background: AnsiBackgroundColor? = null,
                  bold: Boolean = false,
                  underline: Boolean = false,
                  appendReset: Boolean = true): String {
            val values = mutableListOf<Int>()
            if (foreground != null)
                values.add(foreground.value)
            if (background != null)
                values.add(background.value)
            if (bold)
                values.add(AnsiStyle.BOLD.value)
            if (underline)
                values.add(AnsiStyle.UNDERLINE.value)

            val valuesMerged = values.joinToString(";") { it.toString() }
            return "${ESCAPE_CHAR}[${valuesMerged}m$text${if (appendReset) RESET else ""}"
        }
    }
}

enum class AnsiStyle(val value: Int) {
    NORMAL(0),
    BOLD(1),
    UNDERLINE(4);
}

enum class AnsiForegroundColor(val value: Int) {
    GRAY(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    PINK(35),
    CYAN(36),
    WHITE(37);

    companion object {
        fun fromValue(value: Int?): AnsiForegroundColor? {
            if (value == null)
                return null
            return values().firstOrNull { it.value == value }
        }
    }
}

enum class AnsiBackgroundColor(val value: Int) {
    FIREFLY_DARK_BLUE(40),
    ORANGE(41),
    MARBLE_BLUE(42),
    GRAYISH_TURQUOISE(43),
    GRAY(44),
    INDIGO(45),
    LIGHT_GRAY(46),
    WHITE(47);

    companion object {
        fun fromValue(value: Int?): AnsiBackgroundColor? {
            if (value == null)
                return null
            return AnsiBackgroundColor.values().firstOrNull { it.value == value }
        }
    }
}

private fun format(vararg values: Int): String {
    val valuesMerged = values.joinToString(";") { it.toString() }
    return "${DiscordAnsiParser.ESCAPE_CHAR}[${valuesMerged}m"
}

class AnsiStringBuilder internal constructor() {
    val builder = StringBuilder()

    fun fg(foreground: AnsiForegroundColor) {
        builder.append(format(foreground.value))
    }

    fun bg(background: AnsiBackgroundColor) {
        builder.append(format(background.value))
    }

    fun bold() {
        builder.append(format(AnsiStyle.BOLD.value))
    }

    fun underline() {
        builder.append(format(AnsiStyle.UNDERLINE.value))
    }

    fun reset() {
        builder.append(format(0))
    }

    fun append(text: CharSequence) {
        builder.append(text)
    }

    override fun toString() = builder.toString()
}

fun buildAnsi(block: AnsiStringBuilder.() -> Unit) = AnsiStringBuilder().apply(block).toString()