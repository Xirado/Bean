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

package at.xirado.bean.http

import at.xirado.bean.Config
import at.xirado.bean.http.auth.AuthPrincipal
import at.xirado.bean.http.exception.APIException
import at.xirado.bean.http.exception.createErrorResponse
import at.xirado.bean.http.response.error.UnauthorizedError
import at.xirado.bean.http.response.error.respondError
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
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(HttpServer::class.java)

class HttpServer(private val config: Config) {
    private lateinit var engine: NettyApplicationEngine

    init {
        load()
    }

    private fun load() {
        val (host, port) = config.http

        engine = embeddedServer(Netty, host = host, port = port) {
            module()
        }.start(false)
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
                val errorResponse = exception.createErrorResponse()
                logger.error("Encountered API exception", exception)

                call.respondError(errorResponse)
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

        if (config.http.corsAllowAll)
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
                call.respondError(UnauthorizedError)
            }
        }
    }

    private fun Application.routing() = routing {
        discordOAuthCallbackRoute(config.jwt)
        discordCommandsRoute()
        discordInviteUrlRoute()
        discordAuthorizeUrlRoute()

        val authScope = "jwt-discord-oauth"

        authenticate(authScope) {
            guildsRoute()
            guildRoute()
            removeRankedMemberRoute()
        }

        authenticate(authScope, optional = true) {
            leaderboardRoute()
        }
    }
}