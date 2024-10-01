package at.xirado.bean.data.cache

import at.xirado.bean.data.DBContext
import at.xirado.bean.database.entity.Guild
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import net.dv8tion.jda.api.entities.Guild as JDAGuild

private val log = KotlinLogging.logger { }

@Single(createdAtStart = true)
class GuildCacheView(
    private val db: DBContext
) : CacheView<Long, Guild>(1000), KoinComponent {
    suspend fun save(jdaGuild: JDAGuild, edit: Guild.() -> Unit = {}): Guild = db.doTransaction {
        val id = jdaGuild.idLong

        val guild = getById(id)?.apply { updateMetadata(jdaGuild); edit(this) }
            ?: Guild.new(id) { setMetadata(jdaGuild); edit(this) }

        if (id !in this@GuildCacheView)
            put(id, guild)

        guild
    }

    private fun Guild.setMetadata(jdaGuild: JDAGuild) {
        name = jdaGuild.name
        icon = jdaGuild.iconId
        ownerId = jdaGuild.ownerIdLong
    }

    private fun Guild.updateMetadata(jdaGuild: JDAGuild) {
        if (name != jdaGuild.name)
            name = jdaGuild.name
        if (icon != jdaGuild.iconId)
            icon = jdaGuild.iconId
        if (ownerId != jdaGuild.ownerIdLong)
            ownerId = jdaGuild.ownerIdLong
    }

    override suspend fun load(key: Long): Guild? = db.doTransaction {
        Guild.findById(key)
    }
}