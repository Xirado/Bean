package at.xirado.bean.database.table

import org.jetbrains.exposed.dao.id.IdTable

object HashedCommands : IdTable<String>("command_hashes") {
    override val id = varchar("identifier", 100).entityId()
    val hash        = char("hash", 64)
}