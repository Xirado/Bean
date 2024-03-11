package at.xirado.bean.http.error.exception

import at.xirado.bean.http.error.APIError
import at.xirado.bean.http.error.InternalServerError
import at.xirado.bean.http.error.UnauthorizedError

open class APIException(val code: Int, message: String, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(code: Int, message: String) : this(code, message, null)
}

fun APIException.createResponse(): APIError = when (code) {
    401 -> UnauthorizedError
    else -> InternalServerError
}