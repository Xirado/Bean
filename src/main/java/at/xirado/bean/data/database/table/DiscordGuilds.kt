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

import at.xirado.bean.data.ReactionRole
import at.xirado.bean.data.RoleReward
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.json.json

private val jsonConfig = Json {
    ignoreUnknownKeys = true
}

object DiscordGuilds : IdTable<Long>("guilds") {
    override val id = long("guild_id").entityId()

//    val moderatorRoles = array<Long>("moderator_roles", LongColumnType()).asSet()
//    val silentLevelingChannels = array<Long>("silent_leveling_channels", LongColumnType()).asSet()

    val moderatorRoles = json<Set<Long>>("moderator_roles", jsonConfig).clientDefault { emptySet() }
    val silentLevelingChannels = json<Set<Long>>("silent_leveling_channels", jsonConfig).clientDefault { emptySet() }

    val logChannel = long("log_channel").nullable()

    val reactionRoles = json(
        "reaction_roles", jsonConfig, SetSerializer(ReactionRole.serializer())
    ).clientDefault { emptySet() }

    val roleRewards = json(
        "role_rewards", jsonConfig, SetSerializer(RoleReward.serializer())
    ).clientDefault { emptySet() }
}