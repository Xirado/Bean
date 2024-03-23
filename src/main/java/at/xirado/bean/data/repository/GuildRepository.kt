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

    override fun close() {
        guildCache.invalidateAll()

        job.cancel("Shutting down")
    }
}