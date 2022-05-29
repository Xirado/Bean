package at.xirado.bean.data

import at.xirado.bean.io.db.SQLBuilder
import at.xirado.simplejson.JSONObject

class UserData(val guildId: Long, val jsonObject: JSONObject) {

    suspend fun update(): UserData {
        val sql = SQLBuilder("INSERT INTO user_data(user_id, data) values(?, ?::JSONB) ON CONFLICT(user_id) DO UPDATE SET data = excluded.data")
        sql.addParameter(guildId, jsonObject.toString())
        sql.execute()
        return this
    }

    var rankBackground: String
        get() = jsonObject.getString("rank_background", "default")
        set(value) { jsonObject.put("rank_background", value) }
}