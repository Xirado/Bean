package at.xirado.bean.http.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.guildRoute() {
    get("/guilds/{guild}") {
        val guildId = call.parameters["guild"]!!

    }
}