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

package at.xirado.bean.data.repository

import at.xirado.bean.data.database.Database
import at.xirado.bean.data.database.entity.DiscordGuild
import at.xirado.bean.http.oauth.virtualDispatcher
import at.xirado.bean.misc.createCoroutineScope
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.cacheBuilder
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

class GuildRepository(private val database: Database) : AutoCloseable {
    private val job = SupervisorJob()
    private val coroutineScope = createCoroutineScope(virtualDispatcher, job)

    private val guildCache: Cache<Long, DiscordGuild> = cacheBuilder<Long, DiscordGuild> {
        useCallingContext = false
        scope = coroutineScope
        expireAfterAccess = 10.minutes
    }.build()

    fun getGuildDataBlocking(guildId: Long): DiscordGuild {
        return runBlocking {
            guildCache.get(guildId) {
                transaction {
                    DiscordGuild.findById(guildId)
                        ?: DiscordGuild.new(id = guildId) { }
                }
            }
        }
    }

    suspend fun getGuildDataAsync(guildId: Long): DiscordGuild {
        return guildCache.get(guildId) {
            newSuspendedTransaction {
                DiscordGuild.findById(guildId)
                    ?: DiscordGuild.new(id = guildId) { }
            }
        }
    }

    override fun close() {
        guildCache.invalidateAll()

        job.cancel("Shutting down")
    }
}