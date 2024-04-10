package at.xirado.bean.http.response.error

import io.ktor.http.*

object UnauthorizedError : HttpError {
    override val code = HttpStatusCode.Unauthorized
    override val message = "Unauthorized"
}