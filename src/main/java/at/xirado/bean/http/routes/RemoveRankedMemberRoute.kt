package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import at.xirado.bean.data.RankingSystem
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

        val success = RankingSystem.clearXP(guildId, memberId)

        if (!success)
            return@delete call.respondError(noSuchMemberError)

        call.respond(HttpStatusCode.NoContent)
    }
}