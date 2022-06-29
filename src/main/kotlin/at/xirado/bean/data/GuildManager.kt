package at.xirado.bean.data

import at.xirado.bean.Application
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.computeSuspendIfAbsent
import at.xirado.bean.util.getLog
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

private val log = getLog<GuildManager>()

class GuildManager(val application: Application) {
    private val cache = ExpiringMap.builder()
        .expiration(30, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expirationListener { k: Long, _: GuildData ->
            log.debug("Unloaded guild data of $k")
        }
        .build<Long, GuildData>()

    suspend fun getGuildData(guildId: Long): GuildData = cache.computeSuspendIfAbsent(guildId) { retrieveGuildData(it) }

    private suspend fun retrieveGuildData(guildId: Long): GuildData {
        return SQLBuilder("SELECT data FROM guild_data WHERE guild_id = ?", guildId).executeQuery {
            if (it.next())
                return@executeQuery GuildData(guildId, it.getString("data"))
            return@executeQuery GuildData(guildId, "{}")
        }!!.also { log.debug("Loaded guild data of $guildId") }
    }
}