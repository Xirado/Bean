package at.xirado.bean.http.error

object InternalServerError : APIError {
    override val code: Int = 500
    override val message: String = "Internal Server Error"
}