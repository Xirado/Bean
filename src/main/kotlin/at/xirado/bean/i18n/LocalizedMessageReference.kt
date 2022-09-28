package at.xirado.bean.i18n

class LocalizedMessageReference(val path: String): CharSequence {
    fun get(locale: I18n, vararg attributes: Pair<String, Any>) =
        locale.get(path, *attributes)
            ?: locale.manager.default.getValue(path, *attributes)

    override val length: Int
        get() = path.length

    override fun get(index: Int): Char {
        return path[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return path.subSequence(startIndex, endIndex)
    }
}