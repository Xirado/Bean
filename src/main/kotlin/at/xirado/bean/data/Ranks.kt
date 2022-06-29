package at.xirado.bean.data

import at.xirado.bean.APPLICATION
import at.xirado.bean.io.db.SQLBuilder

data class Rank(val name: String, val color: Int, val display: String)

suspend fun getRankFromName(name: String): Rank? {
    return SQLBuilder("SELECT * FROM ranks WHERE name = ?", name).executeQuery { rs ->
        if (rs.next())
            return@executeQuery Rank(rs.getString("name"), rs.getInt("color"), rs.getString("display"))
        return@executeQuery null
    }
}

suspend fun getUserRank(userId: Long): Rank? {
    val rank = APPLICATION.userManager.getUserData(userId).getString("rank", null)?: return null
    return getRankFromName(rank)
}

suspend fun setUserRank(userId: Long, rank: Rank) {
    APPLICATION.userManager.getUserData(userId).update {
        put("rank", rank.name)
    }
}