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

package at.xirado.bean.data.database.entity

import at.xirado.bean.data.database.table.DiscordOAuthSessions
import at.xirado.bean.http.oauth.model.DiscordUser
import at.xirado.bean.http.oauth.model.Guild
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DiscordOAuthSession(id: EntityID<Long>) : Entity<Long>(id) {
    var accessToken by DiscordOAuthSessions.accessToken
    var refreshToken by DiscordOAuthSessions.refreshToken
    var scope by DiscordOAuthSessions.scope
    var expiry by DiscordOAuthSessions.expiry

    val isExpired: Boolean
        get() = System.currentTimeMillis() + 60000 > expiry

    var user: DiscordUser? = null
    var guilds: List<Guild>? = null

    companion object : EntityClass<Long, DiscordOAuthSession>(DiscordOAuthSessions)
}