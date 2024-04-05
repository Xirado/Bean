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
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

fun Route.guildRoute() {
    route("/guilds/{guild}") {
        get {
            val member = checkAuthorization() ?: return@get
            val guild = member.guild

            val includeSettings = call.request.queryParameters["withSettings"] == "true"
            val deep = call.request.queryParameters["deep"] == "true"

            var guildSettingsJson: JsonObject? = null

            if (includeSettings) {
                val guildData = Bean.getInstance().repository.guildRepository.getGuildDataAsync(guild.idLong)

                guildSettingsJson = buildJsonObject {
                    allowedProperties.forEach { (property, serializer) ->
                        val obj = property.get(guildData)
                        val jsonObject = Json.encodeToJsonElement(serializer, obj)
                        put(property.name, jsonObject)
                    }
                }
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

            call.respond(HttpStatusCode.NoContent)
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