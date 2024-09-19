/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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