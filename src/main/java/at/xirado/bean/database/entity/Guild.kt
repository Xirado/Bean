package at.xirado.bean.database.entity

import at.xirado.bean.database.exposed.optionalRelated
import at.xirado.bean.database.table.Guilds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Guild(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Guild>(Guilds)

    var name           by Guilds.name
    var icon           by Guilds.icon
    var ownerId        by Guilds.ownerId
    var flags          by Guilds.flags
    var features       by Guilds.features
    var levelingConfig by optionalRelated(GuildLevelingConfig)

    override fun toString(): String {
        return "Guild(id=$id)"
    }

    fun toString(all: Boolean): String {
        if (!all)
            return toString()

        return "Guild(id=$id, name=$name, ownerId=$ownerId, flags=$flags, features=$features, levelingConfig=$levelingConfig)"
    }
}
