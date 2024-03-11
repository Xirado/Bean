package at.xirado.bean.http.error

data object InternalServerError : APIError {
    override val code: Int = 500
    override val message: String = "Internal Server Error"
}