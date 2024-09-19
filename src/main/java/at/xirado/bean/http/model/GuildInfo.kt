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

package at.xirado.bean.http.model

import at.xirado.bean.http.oauth.model.BotMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

@Serializable
data class GuildInfo(
    val id: String,
    val name: String,
    val iconUrl: String?,
    val settings: JsonObject? = null,
    val channels: List<GuildChannelInfo>? = null,
    val roles: List<GuildRoleInfo>? = null,
    val botMetadata: BotMetadata? = null,
) {
    constructor(guild: Guild, deep: Boolean, settings: JsonObject? = null) : this(
        guild.id,
        guild.name,
        guild.iconUrl,
        settings = settings,
        channels = deep.ifTrue { guild.channels.map(::GuildChannelInfo) },
        roles = deep.ifTrue { guild.roleCache.map(::GuildRoleInfo) },
        botMetadata = BotMetadata(joined = true)
    )

    constructor(guild: at.xirado.bean.http.oauth.model.Guild) : this(
        guild.id,
        guild.name,
        guild.iconUrl,
        botMetadata = guild.botMetadata,
    )
}

@Serializable
data class GuildChannelInfo(
    val id: String,
    val type: Int,
    val position: Int? = null,
    val name: String,
) {
    constructor(channel: GuildChannel) : this(
        channel.id,
        channel.type.id,
        (channel as? IPositionableChannel)?.position,
        channel.name
    )
}

@Serializable
data class GuildRoleInfo(
    val id: String,
    val name: String,
    val color: Int,
    val position: Int,
    val permissions: String,
) {
    constructor(role: Role) : this(
        role.id,
        role.name,
        role.colorRaw,
        role.position,
        role.permissionsRaw.toString(),
    )
}