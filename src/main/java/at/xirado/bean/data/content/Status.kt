package at.xirado.bean.data.content

enum class Status {
    NONE, // No state
    SEEN, // The user has seen the content, but has not interacted with it.
    AWARE, // The user is aware of the feature(s) showcased in this dismissable content.
    ACKNOWLEDGED; // The user explicitly acknowledged this dismissable content.
}
