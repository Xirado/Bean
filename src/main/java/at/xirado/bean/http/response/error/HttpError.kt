package at.xirado.bean.http.response.error

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class HttpErrorResponse(
    val code: Int,
    val message: String,
)

interface HttpError {
    val code: HttpStatusCode

    val message: String
}

suspend fun ApplicationCall.respondError(error: HttpError) {
    val code = error.code
    val message = error.message

    val response = HttpErrorResponse(code.value, message)
    respond(error.code, response)
}