package at.xirado.bean.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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