package at.xirado.bean.http

import at.xirado.bean.HttpServerConfig
import at.xirado.bean.http.auth.AuthPrincipal
import at.xirado.bean.http.error.APIError
import at.xirado.bean.http.error.UnauthorizedError
import at.xirado.bean.http.error.exception.APIException
import at.xirado.bean.http.error.exception.createResponse
import at.xirado.bean.http.routes.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class HttpServer(private val config: HttpServerConfig) {
    private lateinit var engine: NettyApplicationEngine

    init {
        load()
    }

    private fun load() {
        val (host, port) = config
        engine = embeddedServer(Netty, host = host, port = port) {
            module()
        }
    }

    fun stop() {
        engine.stop(500, 5000)
    }

    private fun Application.module() {
        install(ContentNegotiation) {
            json()
        }

        installCors()
        installAuthentication()
        routing()

        install(StatusPages) {
            exception<APIException> { call, exception ->
                val errorResponse = exception.createResponse()
                call.respond(HttpStatusCode.fromValue(errorResponse.code), errorResponse)
            }
        }
    }

    private fun Application.installCors() = install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        if (config.corsAllowAll)
            anyHost()
    }

    private fun Application.installAuthentication() = install(Authentication) {
        jwt("jwt-discord-oauth") {
            val jwtConfig = config.jwt
            realm = jwtConfig.realm

            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credentials ->
                val payload = credentials.payload
                val userId = payload.getClaim("discord-id").asLong()
                    ?: return@validate null

                AuthPrincipal(userId, payload)
            }

            challenge { _, _ ->
                call.respond<APIError>(HttpStatusCode.Unauthorized, UnauthorizedError)
            }
        }
    }

    private fun Application.routing() = routing {
        discordOAuthCallbackRoute(config.jwt)
        discordCommandsRoute()
        discordInviteUrlRoute()
        discordAuthorizeUrlRoute()

        authenticate("jwt-discord-oauth") {
            guildsRoute()
        }
    }
}