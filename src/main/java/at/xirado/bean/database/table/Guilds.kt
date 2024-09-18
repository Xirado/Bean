package at.xirado.bean.database.table

import at.xirado.bean.data.bitField
import at.xirado.bean.model.GuildFeature
import at.xirado.bean.model.GuildFlag
import org.jetbrains.exposed.dao.id.IdTable

object Guilds : IdTable<Long>("guilds") {
    override val id = long("id").entityId()
    val name        = varchar("name", 100)
    val icon        = varchar("icon", 44).nullable()
    val ownerId     = long("owner_id")
    val flags       = short("flags").clientDefault { 0 }.bitField<Short, GuildFlag>()
    val features    = integer("features").bitField<Int, GuildFeature>().nullable()
    override val primaryKey = PrimaryKey(id)
}

