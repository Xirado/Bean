package at.xirado.bean.data.cache

import at.xirado.bean.data.DBContext
import at.xirado.bean.database.entity.Member
import at.xirado.bean.database.table.Members
import org.jetbrains.exposed.dao.id.CompositeID
import org.koin.core.annotation.Single
import net.dv8tion.jda.api.entities.Member as JDAMember

@Single(createdAtStart = true)
class MemberCacheView(
    private val userCacheView: UserCacheView,
    private val guildCacheView: GuildCacheView,
    private val db: DBContext,
) : CacheView<CompositeID, Member>(2000) {
    suspend fun save(jdaMember: JDAMember, edit: Member.() -> Unit = {}): Member = db.doTransaction {
        val user = userCacheView.save(jdaMember.user)
        val guild = guildCacheView.save(jdaMember.guild)
        val id = jdaMember.compositeId()

        val member = getById(id)?.apply(edit)
            ?: Member.new(id) {
                this.user = user
                this.guild = guild
                edit()
            }

        if (id !in this@MemberCacheView)
            put(id, member)

        member
    }

    override suspend fun load(key: CompositeID): Member? = db.doTransaction {
        Member.findById(key)
    }
}

fun JDAMember.compositeId() = CompositeID {
    it[Members.user] = idLong
    it[Members.guild] = guild.idLong
}