package at.xirado.bean.http.error

data object UnauthorizedError : APIError {
    override val code: Int = 401
    override val message: String = "Unauthorized"
}