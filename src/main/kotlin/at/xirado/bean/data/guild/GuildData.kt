package at.xirado.bean.data.guild

import at.xirado.bean.data.DataContainer
import at.xirado.bean.data.JSONProperty
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.getNullable
import at.xirado.simplejson.FileType
import at.xirado.simplejson.JSONObject
import org.intellij.lang.annotations.Language

@Language("SQL")
private val updateSqlStatement = """
    INSERT INTO guild_data(guild_id, data) values(?, ?::JSONB)
    ON CONFLICT(guild_id) DO UPDATE SET data = excluded.data
""".trimIndent()

class GuildData(val guildId: Long, json: String) : DataContainer<GuildData>(json, FileType.JSON) {

    var levelingConfig: LevelingConfig by JSONProperty(
        "leveling",
        LevelingConfig(),
        { obj, delegate -> obj.getNullable<JSONObject>(delegate.key)?.let { LevelingConfig.fromData(it) } },
    )

    override val onUpdate: suspend (JSONObject) -> Unit = { SQLBuilder(updateSqlStatement, guildId, it.toString()).execute() }
}
