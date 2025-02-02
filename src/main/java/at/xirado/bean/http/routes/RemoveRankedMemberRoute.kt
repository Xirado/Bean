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

package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import at.xirado.bean.data.LevelingUtils
import at.xirado.bean.http.HttpServer
import at.xirado.bean.http.auth.AuthPrincipal
import at.xirado.bean.http.response.error.BadRequestError
import at.xirado.bean.http.response.error.NotFoundError
import at.xirado.bean.http.response.error.UnauthorizedError
import at.xirado.bean.http.response.error.respondError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val invalidGuildIdError = BadRequestError("Invalid guild id")
private val noSuchGuildError = NotFoundError("No guild found with this id")
private val invalidMemberIdError = BadRequestError("Invalid user id")
private val noSuchMemberError = NotFoundError("No member found with this id")
private val log = LoggerFactory.getLogger(HttpServer::class.java)

fun Route.removeRankedMemberRoute() {
    delete("/leaderboard/{guild}/members/{member}") {
        val principal = call.principal<AuthPrincipal>()!!

        val guildId = call.parameters["guild"]?.toLongOrNull()
            ?: return@delete call.respondError(invalidGuildIdError)

        val memberId = call.parameters["member"]?.toLongOrNull()
            ?: return@delete call.respondError(invalidMemberIdError)

        val guild = Bean.getInstance().shardManager.getGuildById(guildId)
            ?: return@delete call.respondError(noSuchGuildError)

        if (!canRemoveMembersFromLeaderboard(guild, principal.userId))
            return@delete call.respondError(UnauthorizedError)

        val success = LevelingUtils.clearXP(guildId, memberId)

        if (!success)
            return@delete call.respondError(noSuchMemberError)

        call.respond(HttpStatusCode.NoContent)
    }
}