package at.xirado.bean.http.response.error

import io.ktor.http.*

object InternalServerError : HttpError {
    override val code = HttpStatusCode.InternalServerError
    override val message = "Internal Server Error"
}