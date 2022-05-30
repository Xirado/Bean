package at.xirado.bean.data

import at.xirado.bean.Application
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.computeSuspendIfAbsent
import at.xirado.simplejson.JSONObject
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

class UserManager(val application: Application) {
    private val cache = ExpiringMap.builder()
        .expiration(30, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build<Long, UserData>()

    suspend fun getUserData(userId: Long) = cache.computeSuspendIfAbsent(userId) { retrieveUserData(it) }

    private suspend fun retrieveUserData(userId: Long): UserData {
        return SQLBuilder("SELECT data FROM user_data WHERE user_id = ?", userId).executeQuery {
            if (it.next())
                return@executeQuery UserData(userId, JSONObject.fromJson(it.getString("data")))
            return@executeQuery UserData(userId, JSONObject.empty())
        }!!
    }

}