package at.xirado.bean.database.table

import org.jetbrains.exposed.dao.id.IdTable

object PersistentMessageViews : IdTable<Long>("persistent_views") {
    override val id = long("id").entityId()
    val data        = binary("data")
    val className   = varchar("class_name", 100)

    override val primaryKey = PrimaryKey(id)
}