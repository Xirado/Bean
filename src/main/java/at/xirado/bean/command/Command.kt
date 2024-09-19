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

package at.xirado.bean.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.internal.utils.Checks
import java.util.*

abstract class Command protected constructor(name: String, description: String, usage: String) {

    val name: String
    val description: String
    val usage: String
    private val aliases: MutableList<String> = mutableListOf()
    private val requiredPermissions: MutableList<Permission> = mutableListOf()
    private val requiredBotPermissions: MutableList<Permission> = mutableListOf()
    private val allowedGuilds: MutableList<Long> = mutableListOf()
    private val commandFlags: EnumSet<CommandFlag> = EnumSet.noneOf(CommandFlag::class.java)

    init {
        this.name = name
        this.description = description
        this.usage = usage
    }

    fun addAliases(vararg aliases: String) {
        Checks.notNull(aliases, "Aliases")
        this.aliases.addAll(listOf(*aliases))
    }

    fun addRequiredPermissions(vararg permissions: Permission) {
        Checks.notNull(permissions, "Permissions")
        requiredPermissions.addAll(listOf(*permissions))
    }

    fun addRequiredBotPermissions(vararg permissions: Permission) {
        Checks.notNull(permissions, "Permissions")
        requiredBotPermissions.addAll(listOf(*permissions))
    }

    fun addAllowedGuilds(vararg guildIDs: Long) {
        Checks.notEmpty(guildIDs.toList(), "Guild Ids")
        allowedGuilds.addAll(guildIDs.toList())
    }

    fun addCommandFlags(vararg commandFlags: CommandFlag?) {
        Checks.notNull(commandFlags, "CommandFlags")
        this.commandFlags.addAll(listOf(*commandFlags))
    }

    fun getAliases(): List<String> {
        return Collections.unmodifiableList(aliases)
    }

    fun getRequiredPermissions(): List<Permission> {
        return Collections.unmodifiableList(requiredPermissions)
    }

    fun getRequiredBotPermissions(): List<Permission> {
        return Collections.unmodifiableList(requiredBotPermissions)
    }

    fun getAllowedGuilds(): List<Long> {
        return Collections.unmodifiableList(allowedGuilds)
    }

    fun getCommandFlags(): Set<CommandFlag> {
        return Collections.unmodifiableSet(commandFlags)
    }

    fun hasCommandFlag(flag: CommandFlag): Boolean {
        return commandFlags.contains(flag)
    }

    fun isAvailableIn(guildID: Long): Boolean {
        return if (hasCommandFlag(CommandFlag.PRIVATE_COMMAND)) getAllowedGuilds().contains(guildID) else true
    }

    abstract suspend fun executeCommand(event: MessageReceivedEvent, context: CommandContext)

    override fun toString(): String {
        return "Command{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", usage='" + usage + '\'' +
                ", aliases=" + aliases +
                ", requiredPermissions=" + requiredPermissions +
                ", requiredBotPermissions=" + requiredBotPermissions +
                ", allowedGuilds=" + allowedGuilds +
                ", commandFlags=" + commandFlags +
                '}'
    }
}
