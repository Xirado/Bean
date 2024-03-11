package at.xirado.bean.http.error

import kotlinx.serialization.Serializable

@Serializable
data object UnauthorizedError : APIError {
    override val code: Int = 401
    override val message: String = "Unauthorized"
}