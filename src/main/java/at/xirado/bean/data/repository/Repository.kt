package at.xirado.bean.data.repository

import at.xirado.bean.data.database.Database

class Repository(private val database: Database) {
    val guildRepository = GuildRepository(database)
}