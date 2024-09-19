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