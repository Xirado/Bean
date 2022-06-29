package at.xirado.bean.data

import at.xirado.bean.io.db.SQLBuilder
import at.xirado.simplejson.JSONObject
import org.intellij.lang.annotations.Language
import java.nio.file.Files
import java.nio.file.Path

@Language("SQL")
private val updateSqlStatement = """
    INSERT INTO user_data(user_id, data) values(?, ?::JSONB)
    ON CONFLICT(user_id) DO UPDATE SET data = excluded.data
""".trimIndent()

class UserData(val userId: Long, json: String) : JSONObject(json) {

    var rankBackground: String
        get() = getString("rank_background", "default")
        set(value) { deleteOldBackground().also { put("rank_background", value) } }

    var rankAccentColor: Int
        get() = getUnsignedInt("rank_accent_color", 0x0C71E0)
        set(value) { put("rank_accent_color", value) }

    suspend fun update(block: UserData.() -> Unit) = apply(block).update()

    private suspend fun update(): UserData {
        SQLBuilder(updateSqlStatement, userId, toString()).execute()
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