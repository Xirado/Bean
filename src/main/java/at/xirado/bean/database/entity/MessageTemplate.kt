package at.xirado.bean.database.entity

import at.xirado.bean.data.cache.GuildCacheView
import at.xirado.bean.database.exposed.provideUsing
import at.xirado.bean.database.table.MessageTemplates
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageTemplate(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, MessageTemplate>(MessageTemplates), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
    }

    var guild         by MessageTemplates.guild provideUsing guildCacheView
    var name          by MessageTemplates.name
    var content       by MessageTemplates.content
    var embeds        by MessageTemplates.embeds
}