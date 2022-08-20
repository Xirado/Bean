package at.xirado.bean.i18n

class LocalizedMessage(val i18n: I18n, val path: String, val layout: String, val formatted: String) : CharSequence {
    override val length: Int
        get() = formatted.length

    override fun get(index: Int): Char = formatted[index]

    override fun subSequence(startIndex: Int, endIndex: Int) = formatted.subSequence(startIndex, endIndex)

    override fun toString() = formatted
}