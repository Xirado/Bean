package at.xirado.bean.database.table

import at.xirado.bean.model.Embed
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.json.jsonb

object MessageTemplates : IdTable<Int>("message_templates") {
    override val id = integer("id").entityId()
    val guild       = reference("guild", Guilds).nullable()
    val name        = varchar("name", 100).nullable()
    val content     = varchar("content", 2000).nullable()
    val embeds      = jsonb("embeds", Json, serializer<List<Embed>>()).default(emptyList())
}