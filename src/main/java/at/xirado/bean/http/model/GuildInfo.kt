package at.xirado.bean.http.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

@Serializable
data class GuildInfo(
    val settings: JsonObject? = null,
    val name: String,
    val id: String,
    val iconUrl: String?,
    val channels: List<GuildChannelInfo>? = null,
    val roles: List<GuildRoleInfo>? = null,
) {
    constructor(guild: Guild, deep: Boolean, settings: JsonObject? = null) : this(
        settings,
        guild.name,
        guild.id,
        guild.iconUrl,
        deep.ifTrue { guild.channels.map(::GuildChannelInfo) },
        deep.ifTrue { guild.roleCache.map(::GuildRoleInfo) },
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