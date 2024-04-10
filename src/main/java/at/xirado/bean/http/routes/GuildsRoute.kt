package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import at.xirado.bean.http.auth.AuthPrincipal
import at.xirado.bean.http.model.GuildInfo
import at.xirado.bean.http.oauth.model.BotMetadata
import at.xirado.bean.http.oauth.model.Guild
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.dv8tion.jda.api.Permission

private val discordApi by lazy { Bean.getInstance().discordApi }

fun Route.guildsRoute() {
    get("/guilds") {
        val principal = call.principal<AuthPrincipal>()!!
        val session = discordApi.prepareOAuth(principal.userId)

        val guilds = discordApi.retrieveGuilds(session)
            .filter(::isAdmin)
            .map(::applyBotMetadata)
            .map(::GuildInfo)

        call.respond(guilds)
    }
}

private fun applyBotMetadata(guild: Guild): Guild {
    Bean.getInstance().shardManager.getGuildById(guild.id)
        ?: return guild

    return guild.copy(botMetadata = BotMetadata(joined = true))
}

private fun isAdmin(guild: Guild): Boolean {
    val permissions = Permission.getPermissions(guild.permissions.toLong())

    return Permission.ADMINISTRATOR in permissions || Permission.MANAGE_SERVER in permissions
}

