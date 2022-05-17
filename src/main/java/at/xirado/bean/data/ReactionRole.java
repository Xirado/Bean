package at.xirado.bean.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

public class ReactionRole implements SerializableData {
    private String emote;

    @JsonProperty("message_id")
    private long messageId;

    @JsonProperty("role_id")
    private long roleId;

    public String getEmote() {
        return emote;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public ReactionRole(String emote, long messageId, long roleId) {
        this.emote = emote;
        this.messageId = messageId;
        this.roleId = roleId;
    }

    @NotNull
    @Override
    public DataObject toData() {
        return DataObject.empty()
                .put("role_id", roleId)
                .put("emote", emote)
                .put("message_id", messageId);
    }
}
