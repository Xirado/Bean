package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import at.xirado.bean.http.HttpServer
import at.xirado.bean.http.JWTConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import at.xirado.bean.http.oauth.model.DiscordLoginResponse
import at.xirado.bean.http.oauth.model.OAuthLoginRequest
import java.util.*

private val discordApi by lazy { Bean.getInstance().discordApi }

fun Route.discordOAuthCallbackRoute(jwtConfig: JWTConfig) {
    post("/callback/discord") {
        val body = call.receive<OAuthLoginRequest>()

        val (session, user) = discordApi.newSession(body.code)

        val token = JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("discord-id", session.id.value)
            .withExpiresAt(Date(System.currentTimeMillis() + (1000 * 3600 * 24 * 7)))
            .sign(Algorithm.HMAC256(jwtConfig.secret))

        call.respond(DiscordLoginResponse(token, user))
    }
}