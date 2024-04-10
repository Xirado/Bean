package at.xirado.bean.http.response.error

import io.ktor.http.*

class NotFoundError(override val message: String) : HttpError {
    override val code = HttpStatusCode.NotFound
}