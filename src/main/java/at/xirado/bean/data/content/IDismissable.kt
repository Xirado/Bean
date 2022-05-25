package at.xirado.bean.data.content

interface IDismissable<T> {
    val identifier: String
    val mediaUrl: String?
    val value: T
}
