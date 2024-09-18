package at.xirado.bean.database.table

import at.xirado.bean.data.bitField
import at.xirado.bean.model.UserFlag
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ShortColumnType

object Users : IdTable<Long>("users") {
    override val id = long("id").entityId()
    val username    = varchar("username", 36)
    val displayName = varchar("display_name", 100).nullable()
    val avatar      = varchar("avatar", 44).nullable()
    val banner      = varchar("banner", 44).nullable()
    val flags       = registerColumn("flags", ShortColumnType()).clientDefault { 0 }.bitField<Short, UserFlag>()

    override val primaryKey = PrimaryKey(id)
}