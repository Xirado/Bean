package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import at.xirado.bean.http.auth.AuthPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val discordApi by lazy { Bean.getInstance().discordApi }

fun Route.guildsRoute() {
    get("/guilds") {
        val principal = call.principal<AuthPrincipal>()!!
        val session = discordApi.prepareOAuth(principal.userId)
        val guilds = discordApi.retrieveGuilds(session.accessToken, onlyMutualGuilds = true)

        call.respond(guilds)
    }
}