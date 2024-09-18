package at.xirado.bean.database.table

import org.jetbrains.exposed.dao.id.IdTable

object GuildLevelingConfigs : IdTable<Long>("leveling_configs") {
    override val id            = reference("guild", Guilds)
    val notificationType       = enumeration<NotificationType>("notification_type")
        .default(NotificationType.CURRENT_CHANNEL)
    val levelUpMessageTemplate = reference("level_up_message_template", MessageTemplates).nullable()
    val notificationChannelId  = long("notification_channel_id").nullable()
    val multiplier             = float("multiplier").default(1.0f)
    val strategyId             = short("strategy").default(0)

    override val primaryKey = PrimaryKey(id)
}

enum class NotificationType(val id: Short) {
    CURRENT_CHANNEL(0),
    FIXED_CHANNEL(1),
    DIRECT_MESSAGE(2),
    NONE(3),
}