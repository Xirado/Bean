package at.xirado.bean.data.content

import at.xirado.bean.data.database.SQLBuilder
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.sql.SQLException
import java.util.concurrent.TimeUnit

class DismissableContentManager {
    private val stateCache = ExpiringMap.builder()
        .expiration(5, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build<Long, MutableMap<Feature, DismissableState>>()

    fun setState(
        userId: Long,
        feature: Feature,
        status: Status
    ): DismissableState {
        val state = DismissableState(userId, feature.content, status).update()
        val userCache = stateCache.getOrPut(userId) { mutableMapOf() }
        userCache[feature] = state
        return state
    }

    fun hasState(userId: Long, feature: Feature) = getState(userId, feature) != null

    fun getState(userId: Long, feature: Feature) =
        stateCache.getOrElse(userId) { mutableMapOf() }
            .getOrElse(feature) { fetchState(userId, feature) }

    private fun fetchState(userId: Long, feature: Feature): DismissableState? {
        try {
            SQLBuilder(
                "SELECT * FROM dismissable_contents WHERE user_id = ? AND identifier = ?",
                userId,
                feature.content.identifier
            ).executeQuery()
                .use {
                    if (!it.next()) return null
                    val status = Status.valueOf(it.getString("state"))
                    return DismissableState(userId, feature.content, status)
                }
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }
}
