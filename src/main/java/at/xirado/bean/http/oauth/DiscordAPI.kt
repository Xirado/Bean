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

package at.xirado.bean.http.oauth

import at.xirado.bean.Bean
import at.xirado.bean.OAuthConfig
import at.xirado.bean.data.database.entity.DiscordOAuthSession
import at.xirado.bean.data.database.table.DiscordOAuthSessions
import at.xirado.bean.http.exception.APIException
import at.xirado.bean.http.model.DiscordInviteUrlResponse
import at.xirado.bean.http.model.DiscordLoginUrlResponse
import at.xirado.bean.http.oauth.model.DiscordUser
import at.xirado.bean.http.oauth.model.Guild
import at.xirado.bean.http.oauth.model.OAuthTokenResponse
import at.xirado.bean.misc.createCoroutineScope
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.cacheBuilder
import dev.minn.jda.ktx.util.await
import io.ktor.http.*
import io.ktor.server.util.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.upsert
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.minutes

val virtualExecutor by lazy { Executors.newVirtualThreadPerTaskExecutor() }
val virtualDispatcher by lazy { virtualExecutor.asCoroutineDispatcher() }

private const val BASE_URL = "https://discord.com/api/v10"
private const val TOKEN_ENDPOINT = "$BASE_URL/oauth2/token"

class DiscordAPI(private val config: OAuthConfig) {
    val authorizeUrl = buildAuthorizeUrl()
    val inviteUrl = buildInviteUrl()
    private val json = Json { ignoreUnknownKeys = true }
    private val expiryScope = createCoroutineScope(virtualDispatcher)
    private lateinit var sessionCache: Cache<Long, DiscordOAuthSession>

    init {
        load()
    }

    private fun load() {
        sessionCache = cacheBuilder<Long, DiscordOAuthSession> {
            scope = expiryScope
            expireAfterWrite = 10.minutes
        }.build()
    }

    fun unload() {
        sessionCache.invalidateAll()
    }

    suspend fun prepareOAuth(userId: Long): DiscordOAuthSession {
        return withContext(virtualDispatcher) {
            val session = getOAuthSession(userId)

            refresh(session)
            session
        }
    }

    suspend fun newSession(code: String): Pair<DiscordOAuthSession, DiscordUser> {
        return withContext(virtualDispatcher) {
            val tokenResponse = exchangeCode(code)
            val user = retrieveUser(tokenResponse.accessToken)
            val userId = user.id.toLong()

            newSuspendedTransaction {
                DiscordOAuthSessions.upsert {
                    it[id] = userId
                    it[accessToken] = tokenResponse.accessToken
                    it[refreshToken] = tokenResponse.refreshToken
                    it[scope] = tokenResponse.scope
                    it[expiry] = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000)
                }
            }

            getOAuthSession(userId) to user
        }
    }

    private suspend fun getOAuthSession(userId: Long): DiscordOAuthSession {
        val cachedValue = sessionCache.getIfPresent(userId)
        if (cachedValue != null)
            return cachedValue

        val dbValue = newSuspendedTransaction {
            DiscordOAuthSession.findById(userId)
        } ?: throw APIException(401, "Unauthorized")

        sessionCache.put(userId, dbValue)

        return dbValue
    }

    private suspend fun refresh(session: DiscordOAuthSession) {
        if (!session.isExpired)
            return

        val response = refreshToken(session.refreshToken)

        newSuspendedTransaction {
            session.apply {
                accessToken = response.accessToken
                refreshToken = response.refreshToken
                expiry = (response.expiresIn * 1000) + System.currentTimeMillis()
            }
        }
    }

    private suspend fun exchangeCode(code: String): OAuthTokenResponse {
        val body = FormBody.Builder().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("redirect_uri", config.redirectUri)
            add("client_id", config.clientId.toString())
            add("client_secret", config.clientSecret)
        }.build()

        val request = Request.Builder().apply {
            url(TOKEN_ENDPOINT)
            post(body)
        }.build()

        return makeTokenRequest(request)
    }

    private suspend fun retrieveUser(accessToken: String): DiscordUser {
        val request = Request.Builder().apply {
            url("$BASE_URL/users/@me")
            header("Authorization", "Bearer $accessToken")
            header("User-Agent", "Bean Discord Bot (1.0.0, https://bean.bz)")
        }.build()

        return withContext(virtualDispatcher) {
            Bean.getInstance().okHttpClient.newCall(request).await().use {
                if (!it.isSuccessful)
                    throw newApiException(it)

                val responseBodyString = it.body!!.string()

                json.decodeFromString<DiscordUser>(responseBodyString)
            }
        }
    }

    suspend fun retrieveUser(session: DiscordOAuthSession): DiscordUser {
        val user = retrieveUser(session.accessToken)
        session.user = user
        return user
    }

    private suspend fun retrieveGuilds(accessToken: String, onlyMutualGuilds: Boolean = true): List<Guild> {
        val request = Request.Builder().apply {
            url("$BASE_URL/users/@me/guilds")
            header("Authorization", "Bearer $accessToken")
            header("User-Agent", "Bean Discord Bot (1.0.0, https://bean.bz)")
        }.build()

        return withContext(virtualDispatcher) {
            Bean.getInstance().okHttpClient.newCall(request).await().use {
                if (!it.isSuccessful)
                    throw newApiException(it)

                val responseBodyString = it.body!!.string()

                val guilds = json.decodeFromString<List<Guild>>(responseBodyString)

                if (onlyMutualGuilds) {
                    guilds.filter { guild -> Bean.getInstance().shardManager.getGuildById(guild.id) != null }
                } else {
                    guilds
                }
            }
        }
    }

    suspend fun retrieveGuilds(session: DiscordOAuthSession): List<Guild> {
        session.guilds?.let { return it }

        val guilds = retrieveGuilds(session.accessToken, false)
        session.guilds = guilds

        return guilds
    }

    private suspend fun refreshToken(refreshToken: String): OAuthTokenResponse {
        val body = FormBody.Builder().apply {
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken)
            add("client_id", config.clientId.toString())
            add("client_secret", config.clientSecret)
        }.build()

        val request = Request.Builder().apply {
            url(TOKEN_ENDPOINT)
            post(body)
        }.build()

        return makeTokenRequest(request)
    }

    private suspend fun makeTokenRequest(request: Request): OAuthTokenResponse {
        return Bean.getInstance().okHttpClient.newCall(request).await().use {
            if (!it.isSuccessful)
                throw newApiException(it)

            val responseBodyString = it.body!!.string()
            json.decodeFromString<OAuthTokenResponse>(responseBodyString)
        }
    }

    private fun buildAuthorizeUrl() = url {
        protocol = URLProtocol.HTTPS
        host = "discord.com"
        path("api", "oauth2", "authorize")

        parameters.apply {
            append("response_type", "code")
            append("client_id", config.clientId.toString())
            append("scope", "identify guilds")
            append("permissions", "0")
            append("redirect_uri", config.redirectUri)
            append("prompt", "none")
        }
    }.let { DiscordLoginUrlResponse(it) }

    private fun buildInviteUrl() = url {
        protocol = URLProtocol.HTTPS
        host = "discord.com"
        path("oauth2", "authorize")

        parameters.apply {
            append("client_id", config.clientId.toString())
            append("scope", "bot")
            append("permissions", "275191770223")
        }
    }.let { DiscordInviteUrlResponse(it) }
}

private fun newApiException(response: Response): APIException {
    val status = response.code
    val body = response.body?.string() ?: "N/A"
    return APIException(status, body)
}