package at.xirado.bean.data

import com.fasterxml.jackson.annotation.JsonProperty
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData

class ReactionRole(
    var emote: String,
    @JsonProperty("message_id") var messageId: Long,
    @JsonProperty("role_id") var roleId: Long
) : SerializableData {
    override fun toData() =
        DataObject.empty()
            .put("role_id", roleId)
            .put("emote", emote)
            .put("message_id", messageId)
}
