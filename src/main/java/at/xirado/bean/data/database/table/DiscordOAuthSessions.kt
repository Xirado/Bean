package at.xirado.bean.data.database.table

import org.jetbrains.exposed.dao.id.IdTable

object DiscordOAuthSessions : IdTable<Long>("discord_oauth_sessions") {
    override val id = long("user_id").entityId()
    val accessToken = varchar("access_token", 64)
    val refreshToken = varchar("refresh_token", 64)
    val scope = varchar("scope", 32)
    val expiry = long("expiry")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}