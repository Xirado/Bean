package at.xirado.bean.data

import at.xirado.bean.io.db.SQLBuilder
import at.xirado.simplejson.JSONObject

class GuildData(val guildId: Long, val jsonObject: JSONObject) {

    suspend fun update(): GuildData {
        val sql = SQLBuilder("INSERT INTO guild_data(guild_id, data) values(?, ?::JSONB) ON CONFLICT(guild_id) DO UPDATE SET data = excluded.data")
        sql.addParameter(guildId, jsonObject.toString())
        sql.execute()
        return this
    }
}