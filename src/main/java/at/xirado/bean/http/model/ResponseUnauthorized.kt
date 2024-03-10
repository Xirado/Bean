package at.xirado.bean.http.model

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUnauthorized(
    val error: Int = HttpStatusCode.Unauthorized.value,
    val message: String = "Token invalid or expired",
)
