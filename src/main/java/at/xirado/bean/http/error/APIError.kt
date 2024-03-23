package at.xirado.bean.http.error

import kotlinx.serialization.Serializable

@Serializable
sealed interface APIError {
    val code: Int
    val message: String
}