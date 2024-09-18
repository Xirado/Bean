package at.xirado.bean.database.entity

import at.xirado.bean.database.table.PersistentMessageViews
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PersistentMessageView(id: EntityID<Long>) : Entity<Long>(id) {
    var data: ByteArray by PersistentMessageViews.data
    var className: String by PersistentMessageViews.className
    companion object : EntityClass<Long, PersistentMessageView>(PersistentMessageViews)
}