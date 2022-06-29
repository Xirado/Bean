package at.xirado.bean.data

import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.arrayOrEmpty
import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import org.intellij.lang.annotations.Language
import java.util.stream.Collectors

@Language("SQL")
private val updateSqlStatement = """
    INSERT INTO guild_data(guild_id, data) values(?, ?::JSONB)
    ON CONFLICT(guild_id) DO UPDATE SET data = excluded.data
""".trimIndent()

class GuildData(val guildId: Long, json: String) : JSONObject(json) {

    var minimumExperience: Int
       get() =      getInt("experience_min", 15)
       set(value) { put("experience_min", value) }

    var maximumExperience: Int
        get() =      getInt("experience_max", 25)
        set(value) { put("experience_max", value) }

    var blacklistedLevelingChannels: Set<Long>
        get() =      arrayOrEmpty("blacklisted_leveling_channels").stream(JSONArray::getLong).collect(Collectors.toSet())
        set(value) { put("blacklisted_leveling_channels", value) }

    var djRoles: Set<Long>
        get() =      arrayOrEmpty("dj_roles").stream(JSONArray::getLong).collect(Collectors.toSet())
        set(value) { put("dj_roles", value) }

    suspend fun update(block: GuildData.() -> Unit) = apply(block).update()

    private suspend fun update(): GuildData {
        SQLBuilder(updateSqlStatement, guildId, toString()).execute()
        return this
    }
}