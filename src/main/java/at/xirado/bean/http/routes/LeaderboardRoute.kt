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
import at.xirado.bean.http.auth.AuthPrincipal
import at.xirado.bean.http.model.GuildInfo
import at.xirado.bean.http.response.error.BadRequestError
import at.xirado.bean.http.response.error.NotFoundError
import at.xirado.bean.http.response.error.respondError
import at.xirado.bean.ktx.letAsync
import dev.minn.jda.ktx.coroutines.await
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.exceptions.ErrorResponseException

private val invalidGuildIdError = BadRequestError("Invalid guild id")
private val noSuchGuildError = NotFoundError("No guild found with this id")

fun Route.leaderboardRoute() {
    get("/leaderboard/{guild}") {
        val principal = call.principal<AuthPrincipal>()

        val guildId = call.parameters["guild"]?.toLongOrNull()
            ?: return@get call.respondError(invalidGuildIdError)

        val guild = Bean.getInstance().shardManager.getGuildById(guildId)
            ?: return@get call.respondError(noSuchGuildError)

        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val guildInfo = GuildInfo(guild, deep = false)

        val isAdminJob = principal?.letAsync {
            canRemoveMembersFromLeaderboard(guild, it.userId)
        }

        val rankedMembersJob = async(Dispatchers.IO) {
            LevelingUtils.getLeaderboard(guild.idLong, page, 100)
        }

        val isAdmin = isAdminJob?.await()
        val rankedMembers = rankedMembersJob.await()

        call.respond(GuildLeaderboard(guildInfo, rankedMembers, page, isAdmin = isAdmin))
    }
}

suspend fun canRemoveMembersFromLeaderboard(guild: Guild, userId: Long): Boolean {
    return try {
        val member = guild.getMemberById(userId)
            ?: withContext(Dispatchers.IO) {
                guild.retrieveMemberById(userId).await()
            }

        member.hasPermission(Permission.MANAGE_SERVER)
    } catch (e: ErrorResponseException) {
        false
    }
}

@Serializable
data class GuildLeaderboard(
    val guild: GuildInfo,
    val members: List<RankedMember>,
    val page: Int,
    val isAdmin: Boolean? = null,
)

@Serializable
data class RankedMember(
    val id: String,
    val xp: String,
    val name: String,
    val discriminator: String,
    val avatarUrl: String? = null,
)