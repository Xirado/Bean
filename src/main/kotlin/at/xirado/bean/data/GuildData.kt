package at.xirado.bean.data

import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.arrayOrEmpty
import at.xirado.simplejson.FileType
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import org.intellij.lang.annotations.Language
import java.util.stream.Collectors

@Language("SQL")
private val updateSqlStatement = """
    INSERT INTO guild_data(guild_id, data) values(?, ?::JSONB)
    ON CONFLICT(guild_id) DO UPDATE SET data = excluded.data
""".trimIndent()

class GuildData(val guildId: Long, json: String) : JSONObject(json, FileType.JSON) {

    var minimumExperience: Int by JSONDelegate("experience_min", 15)

    var maximumExperience: Int by JSONDelegate("experience_max", 25)

    var blacklistedLevelingChannels: Set<Long> by JSONDelegate(
        "blacklisted_leveling_channels",
        emptySet(),
        mapArrayToLongSet()
    )

    var djRoles: Set<Long> by JSONDelegate(
        "dj_roles",
        emptySet(),
        mapArrayToLongSet()
    )

    suspend fun update(block: GuildData.() -> Unit) = apply(block).update()

    private suspend fun update(): GuildData {
        SQLBuilder(updateSqlStatement, guildId, toString()).execute()
        return this
    }

    private inline fun <reified T> mapArrayToLongSet(): (JSONDelegate<T>) -> T = { arrayOrEmpty(it.key).stream(JSONArray::getLong).collect(Collectors.toSet()) as T }
}
