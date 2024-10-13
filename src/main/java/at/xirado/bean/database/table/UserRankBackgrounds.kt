package at.xirado.bean.database.table

import org.jetbrains.exposed.dao.id.IdTable

object UserRankBackgrounds : IdTable<Long>("user_rank_backgrounds") {
    override val id = reference("user", Users)
    val background = varchar("background", 64).nullable()
    val color = integer("color").nullable()

    override val primaryKey = PrimaryKey(id)
}