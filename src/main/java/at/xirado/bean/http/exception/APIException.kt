package at.xirado.bean.http.exception

import at.xirado.bean.http.response.error.ForbiddenError
import at.xirado.bean.http.response.error.HttpError
import at.xirado.bean.http.response.error.InternalServerError
import at.xirado.bean.http.response.error.UnauthorizedError

open class APIException(val code: Int, message: String, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(code: Int, message: String) : this(code, message, null)
}

fun APIException.createErrorResponse(): HttpError = when (code) {
    401 -> UnauthorizedError
    403 -> ForbiddenError("Forbidden")
    else -> InternalServerError
}