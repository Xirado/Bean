package at.xirado.bean.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ReactionRole implements Serializable
{
    private String emote;

    @JsonProperty("message_id")
    private long messageId;

    @JsonProperty("role_id")
    private long roleId;

    public String getEmote()
    {
        return emote;
    }

    public long getMessageId()
    {
        return messageId;
    }

    public long getRoleId()
    {
        return roleId;
    }

    public void setEmote(String emote)
    {
        this.emote = emote;
    }

    public void setMessageId(long messageId)
    {
        this.messageId = messageId;
    }

    public void setRoleId(long roleId)
    {
        this.roleId = roleId;
    }
}
