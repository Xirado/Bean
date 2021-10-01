package at.xirado.bean.misc.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class RoleReward implements Serializable
{
    @JsonProperty("level")
    private int level;
    @JsonProperty("role_id")
    private long roleId;
    @JsonProperty("persists")
    private boolean persist;
    @JsonProperty("remove_on_next_reward")
    private boolean removeOnNextReward;

    public RoleReward(int level, long roleId, boolean persist, boolean removeOnNextReward)
    {
        this.level = level;
        this.roleId = roleId;
        this.persist = persist;
        this.removeOnNextReward = removeOnNextReward;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(level);
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(long roleId)
    {
        this.roleId = roleId;
    }

    public boolean isPersistant()
    {
        return persist;
    }

    public void setPersist(boolean persist)
    {
        this.persist = persist;
    }

    public boolean doesRemoveOnNextReward()
    {
        return removeOnNextReward;
    }

    public void setRemoveOnNextReward(boolean removeOnNextReward)
    {
        this.removeOnNextReward = removeOnNextReward;
    }
}
