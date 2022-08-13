package at.xirado.bean.data.user

import at.xirado.bean.data.DataContainer
import at.xirado.bean.data.JSONProperty
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.getNullable
import at.xirado.simplejson.FileType
import at.xirado.simplejson.JSONObject
import org.intellij.lang.annotations.Language

@Language("SQL")
private val updateSqlStatement = """
    INSERT INTO user_data(user_id, data) values(?, ?::JSONB)
    ON CONFLICT(user_id) DO UPDATE SET data = excluded.data
""".trimIndent()

class UserData(val userId: Long, json: String) : DataContainer<UserData>(json, FileType.JSON) {

    var rankCardConfig: RankCardConfig by JSONProperty(
        "rank_card",
        RankCardConfig(),
        { obj, delegate -> obj.getNullable<JSONObject>(delegate.key)?.let { RankCardConfig.fromData(it) } }
    )

    override val onUpdate: suspend (JSONObject) -> Unit = { SQLBuilder(updateSqlStatement, userId, it.toString()).execute() }
}