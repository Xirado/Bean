package at.xirado.bean.misc.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.io.Serializable;

public class RoleReward implements Serializable {
    @JsonProperty("level")
    private int level;
    @JsonProperty("role_id")
    private long roleId;
    @JsonProperty("persists")
    private boolean persist;
    @JsonProperty("remove_on_next_reward")
    private boolean removeOnNextReward;

    public RoleReward(int level, long roleId, boolean persist, boolean removeOnNextReward) {
        this.level = level;
        this.roleId = roleId;
        this.persist = persist;
        this.removeOnNextReward = removeOnNextReward;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public boolean isPersistant() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public boolean doesRemoveOnNextReward() {
        return removeOnNextReward;
    }

    public void setRemoveOnNextReward(boolean removeOnNextReward) {
        this.removeOnNextReward = removeOnNextReward;
    }

    public static RoleReward fromData(DataObject object) {
        return new RoleReward(object.getInt("level"), object.getLong("role_id"), object.isNull("persistant") ? object.getBoolean("persists") : object.getBoolean("persistant"), object.getBoolean("remove_on_next_reward"));
    }
}
