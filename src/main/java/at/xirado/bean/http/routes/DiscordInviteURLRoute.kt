package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val discordApi by lazy { Bean.getInstance().discordApi }

fun Routing.discordInviteUrlRoute() {
    get("/invite") {
        call.respond(discordApi.inviteUrl)
    }
}