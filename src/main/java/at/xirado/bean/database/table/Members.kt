package at.xirado.bean.database.table

import org.jetbrains.exposed.dao.id.CompositeIdTable

object Members : CompositeIdTable("members") {
    val user       = reference("user", Users)
    val guild      = reference("guild", Guilds)
    val experience = integer("experience").default(0)

    init {
        addIdColumn(user)
        addIdColumn(guild)
    }

    override val primaryKey = PrimaryKey(user, guild)
}