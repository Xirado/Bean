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