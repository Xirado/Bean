package at.xirado.bean.database.table

import org.jetbrains.exposed.sql.Table

object GuildCommands : Table("guild_commands") {
    val guild         = long("guild")
    val identifier    = varchar("identifier", 100)
    val commandHash   = char("hash", 64)

    override val primaryKey: PrimaryKey = PrimaryKey(guild, identifier)
}