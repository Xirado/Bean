package at.xirado.bean.http.response.error

import io.ktor.http.*

class ForbiddenError(override val message: String) : HttpError {
    override val code = HttpStatusCode.Forbidden
}