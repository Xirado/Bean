package at.xirado.bean.data

import at.xirado.bean.Application
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.computeSuspendIfAbsent
import at.xirado.simplejson.JSONObject
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

class GuildManager(val application: Application) {
    private val cache = ExpiringMap.builder()
        .expiration(30, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build<Long, GuildData>()

    suspend fun getGuildData(guildId: Long): GuildData = cache.computeSuspendIfAbsent(guildId) { retrieveGuildData(it) }

    private suspend fun retrieveGuildData(guildId: Long): GuildData {
        return SQLBuilder("SELECT data FROM guild_data WHERE guild_id = ?", guildId).executeQuery {
            if (it.next())
                return@executeQuery GuildData(guildId, JSONObject.fromJson(it.getString("data")))
            return@executeQuery GuildData(guildId, JSONObject.empty())
        }!!
    }

}