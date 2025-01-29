package at.xirado.bean.database.entity

import at.xirado.bean.database.table.GuildLevelingConfigs
import at.xirado.bean.leveling.strategy.Mee6LevelingStrategy
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildLevelingConfig(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, GuildLevelingConfig>(GuildLevelingConfigs)

    var notificationType         by GuildLevelingConfigs.notificationType
    var levelUpMessageTemplateId by GuildLevelingConfigs.levelUpMessageTemplate
    var notificationChannelId    by GuildLevelingConfigs.notificationChannelId
    var multiplier               by GuildLevelingConfigs.multiplier
    var strategyId               by GuildLevelingConfigs.strategyId

    fun getStrategy() = when (strategyId.toInt()) {
        0 -> Mee6LevelingStrategy
        else -> throw IllegalStateException("No leveling strategy of id $strategyId")
    }

    override fun toString(): String {
        return "GuildLevelingConfig(id=$id, notificationType=$notificationType)"
    }
}