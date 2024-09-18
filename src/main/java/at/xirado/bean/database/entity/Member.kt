package at.xirado.bean.database.entity

import at.xirado.bean.data.cache.GuildCacheView
import at.xirado.bean.data.cache.UserCacheView
import at.xirado.bean.database.exposed.provideUsing
import at.xirado.bean.database.table.Members
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Member(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<Member>(Members), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
        private val userCacheView by inject<UserCacheView>()
    }

    var user by Members.user provideUsing userCacheView
    var guild by Members.guild provideUsing guildCacheView
    var experience by Members.experience

    override fun toString(): String {
        return "Member(user=$user, guild=$guild)"
    }
}