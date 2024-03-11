package at.xirado.bean.data.database.entity

import at.xirado.bean.data.database.table.DiscordOAuthSessions
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DiscordOAuthSession(id: EntityID<Long>) : Entity<Long>(id) {
    var accessToken by DiscordOAuthSessions.accessToken
    var refreshToken by DiscordOAuthSessions.refreshToken
    var scope by DiscordOAuthSessions.scope
    var expiry by DiscordOAuthSessions.expiry

    val isExpired: Boolean
        get() = System.currentTimeMillis() + 60000 < expiry

    companion object : EntityClass<Long, DiscordOAuthSession>(DiscordOAuthSessions)
}