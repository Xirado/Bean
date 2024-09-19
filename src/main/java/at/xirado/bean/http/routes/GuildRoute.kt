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
import at.xirado.bean.data.database.entity.DiscordGuild
import at.xirado.bean.data.database.entity.UserMutable
import at.xirado.bean.http.auth.AuthPrincipal
import at.xirado.bean.http.model.GuildInfo
import at.xirado.bean.http.response.error.BadRequestError
import at.xirado.bean.http.response.error.ForbiddenError
import at.xirado.bean.http.response.error.NotFoundError
import at.xirado.bean.http.response.error.respondError
import dev.minn.jda.ktx.coroutines.await
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

@Suppress("UNCHECKED_CAST")
private val allowedProperties = DiscordGuild::class.declaredMemberProperties
    .filter { it.hasAnnotation<UserMutable>() }
    .map { it as KMutableProperty1<DiscordGuild, Any?> }
    .associateWith { serializer(it.returnType) }

private val invalidGuildIdError = BadRequestError("Invalid guild id")
private val noSuchGuildError = NotFoundError("No guild found with this id")
private val noAccessError = ForbiddenError("You are not authorized to interact with this entity.")
private val badBodyError = BadRequestError("Invalid body")

@OptIn(ExperimentalSerializationApi::class)
fun Route.guildRoute() {
    route("/guilds/{guild}") {
        get {
            val member = checkAuthorization() ?: return@get
            val guild = member.guild

            val includeSettings = call.request.queryParameters["withSettings"] == "true"
            val deep = call.request.queryParameters["deep"] == "true"

            val guildSettingsJson = includeSettings.ifTrue {
                val guildData = Bean.getInstance().repository.guildRepository.getGuildDataAsync(guild.idLong)
                createSanitizedGuildSettings(guildData)
            }

            call.respond(GuildInfo(guild, deep, guildSettingsJson))
        }

        patch {
            val member = checkAuthorization() ?: return@patch
            val guild = member.guild

            val body = try {
                val bodyString = call.receive<String>()
                Json.decodeFromString<JsonObject>(bodyString)
            } catch (e: Exception) {
                return@patch call.respondError(badBodyError)
            }

            val guildData = Bean.getInstance().repository.guildRepository.getGuildDataAsync(guild.idLong)

            val properties = body.map { (key, element) ->
                val (property, serializer) = allowedProperties.entries.find { it.key.name == key }
                    ?: return@patch call.respondError(BadRequestError("Invalid property named \"$key\""))

                if (!serializer.descriptor.isNullable && element is JsonNull)
                    return@patch call.respondError(BadRequestError("\"$key\" cannot be null"))

                try {
                    property to Json.decodeFromJsonElement(serializer, element)
                } catch (e: Exception) {
                    return@patch call.respondError(BadRequestError("Can't serialize \"$key\" to the expected type."))
                }
            }

            newSuspendedTransaction(db = Bean.getInstance().database.exposed) {
                properties.forEach { (property, element) ->
                    property.set(guildData, element)
                }
            }

            val sanitizedSettings = createSanitizedGuildSettings(guildData)

            call.respond(call.respond(GuildInfo(guild, deep = false, sanitizedSettings)))
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.checkAuthorization(): Member? {
    val principal = call.principal<AuthPrincipal>()!!
    val userId = principal.userId

    val guildId = call.parameters["guild"]?.toLongOrNull()
        ?: return call.respondError(invalidGuildIdError).let { null }

    val guild = Bean.getInstance().shardManager.getGuildById(guildId)
        ?: return call.respondError(noSuchGuildError).let { null }

    val member = try {
        guild.retrieveMemberById(userId).await()
    } catch (e: ErrorResponseException) {
        return call.respondError(noAccessError).let { null }
    }

    if (!member.hasPermission(Permission.MANAGE_SERVER) && !member.hasPermission(Permission.ADMINISTRATOR))
        return call.respondError(noAccessError).let { null }

    return member
}

private fun createSanitizedGuildSettings(guild: DiscordGuild) = buildJsonObject {
    allowedProperties.forEach { (property, serializer) ->
        val obj = property.get(guild)
        val jsonObject = Json.encodeToJsonElement(serializer, obj)
        put(property.name, jsonObject)
    }
}