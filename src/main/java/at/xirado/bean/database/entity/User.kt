package at.xirado.bean.database.entity

import at.xirado.bean.database.exposed.optionalRelated
import at.xirado.bean.database.table.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, User>(Users)

    var username       by Users.username
    var displayName    by Users.displayName
    var avatar         by Users.avatar
    var banner         by Users.banner
    var flags          by Users.flags
    var rankBackground by optionalRelated(UserRankBackground)

    override fun toString(): String {
        return "User(id=$id)"
    }
}