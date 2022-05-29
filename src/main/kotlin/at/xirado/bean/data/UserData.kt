package at.xirado.bean.data

import at.xirado.bean.io.db.SQLBuilder
import at.xirado.simplejson.JSONObject
import java.nio.file.Files
import java.nio.file.Path

class UserData(val userId: Long, val jsonObject: JSONObject) {

    var rankBackground: String
        get() = jsonObject.getString("rank_background", "default")
        set(value) { deleteOldBackground().also { jsonObject.put("rank_background", value) } }

    var rankAccentColor: Int
        get() = jsonObject.getUnsignedInt("rank_accent_color", 0x0C71E0)
        set(value) { jsonObject.put("rank_accent_color", value) }

    suspend fun update(): UserData {
        val sql = SQLBuilder("INSERT INTO user_data(user_id, data) values(?, ?::JSONB) ON CONFLICT(user_id) DO UPDATE SET data = excluded.data")
        sql.addParameter(userId, jsonObject.toString())
        sql.execute()
        return this
    }

    private fun deleteOldBackground() {
        val oldBackground = rankBackground
        if (oldBackground == "default")
            return
        val path = Path.of("backgrounds", oldBackground)
        if (path.toFile().exists())
            runCatching { Files.delete(path) }
    }
}