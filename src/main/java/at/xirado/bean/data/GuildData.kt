package at.xirado.bean.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class GuildData(
    @SerialName("reaction_roles")
    var reactionRoles: MutableSet<ReactionRole> = mutableSetOf(),
    @SerialName("moderator_roles")
    var moderatorRoles: MutableSet<Long> = mutableSetOf(),
    @SerialName("role_rewards")
    val roleRewards: MutableSet<RoleReward> = mutableSetOf(),
    @SerialName("log_channel")
    var logChannel: Long? = null,
)

@Serializable
data class ReactionRole(
    val emote: String,
    @SerialName("message_id")
    val messageId: Long,
    @SerialName("role_id")
    val roleId: Long,
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class RoleReward(
    val level: Int,
    @SerialName("role_id")
    val roleId: Long,
    @JsonNames("persists")
    val persistant: Boolean,
    @SerialName("remove_on_next_reward")
    val removeOnNextReward: Boolean,
)