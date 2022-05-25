package at.xirado.bean.data.content

enum class Feature(val content: IDismissable<*>) {
    CUSTOM_RANK_BACKGROUND(CustomRankBackgroundContent),
    BOOKMARK(BookmarkContent);

    companion object {
        @JvmStatic
        fun fromIdentifier(identifier: String) =
            Feature.values().first { it.content.identifier == identifier }
    }
}
