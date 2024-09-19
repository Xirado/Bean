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