package at.xirado.bean.http.routes

import at.xirado.bean.Bean
import at.xirado.bean.command.BasicSlashCommand
import at.xirado.bean.command.SlashCommand
import at.xirado.bean.command.toBasicCommand
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

private const val devGuildId = 815597207617142814

private val commands by lazy { loadCommands() }

fun Routing.discordCommandsRoute() {
    get("/commands") {
        call.respond(commands)
    }
}

private fun loadCommands(): List<BasicSlashCommand> {
    val commands = when {
        Bean.getInstance().config.debugMode -> Bean.getInstance().interactionHandler.getGuildCommands()[devGuildId] ?: emptyList()
        else -> Bean.getInstance().interactionHandler.getPublicCommands()
    }.mapNotNull { it as? SlashCommand }

    val commandData = commands.map { it.commandData as SlashCommandData }

    return commandData.map(SlashCommandData::toBasicCommand)
}