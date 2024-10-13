package at.xirado.bean.database.entity

import at.xirado.bean.database.table.UserRankBackgrounds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserRankBackground(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, UserRankBackground>(UserRankBackgrounds)

    var background by UserRankBackgrounds.background
    var color      by UserRankBackgrounds.color
}