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
import at.xirado.bean.JWTConfig
import at.xirado.bean.http.oauth.model.DiscordLoginResponse
import at.xirado.bean.http.oauth.model.OAuthLoginRequest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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