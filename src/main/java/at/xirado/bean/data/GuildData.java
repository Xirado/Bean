package at.xirado.bean.data;


import at.xirado.bean.Bean;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.objects.RoleReward;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuildData
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildData.class);

    private final long guildID;
    private final DataObject dataObject;

    private Set<ReactionRole> reactionRoles = new HashSet<>();

    public GuildData(long guildID, DataObject json)
    {
        this.guildID = guildID;
        this.dataObject = json;
        ReactionRole[] reactions = json.isNull("reaction_roles") ? new ReactionRole[0] : json.getArray("reaction_roles")
                .stream(DataArray::getObject)
                .map(object -> new ReactionRole(object.getString("emote"), object.getLong("message_id"), object.getLong("role_id")))
                .toArray(ReactionRole[]::new);
        if (reactions.length > 0)
            reactionRoles.addAll(Arrays.asList(reactions));
    }

    public GuildData put(String key, Object object)
    {
        dataObject.put(key, object);
        return this;
    }

    public String toJson()
    {
        return dataObject.toString();
    }

    public String toPrettyString()
    {
        return dataObject.toPrettyString();
    }

    public GuildData putNull(String key)
    {
        dataObject.putNull(key);
        return this;
    }

    public boolean isNull(String key)
    {
        return dataObject.isNull(key);
    }

    public String getString(String query, Object... objects)
    {
        return String.format(dataObject.getString(query), objects);
    }

    public Integer getInt(String query)
    {
        return dataObject.getInt(query);
    }

    public Boolean getBoolean(String query)
    {
        return dataObject.getBoolean(query);
    }

    public Double getDouble(String query)
    {
        return dataObject.getDouble(query);
    }

    public Long getLong(String query)
    {
        return dataObject.getLong(query);
    }

    public Object getObject(String query)
    {
        return dataObject.getObject(query);
    }

    public <T> T get(String query, Class<T> type)
    {
        return type.cast(dataObject.get(query));
    }

    // guild specific getters and setters

    public GuildData update()
    {
        String sql = "INSERT INTO guildSettings (guildID, data) values (?,?) ON DUPLICATE KEY UPDATE data = ?";
        try
        {
            String jsonString = dataObject.toString();
            new SQLBuilder(sql)
                    .addParameters(guildID, jsonString, jsonString)
                    .execute();
        }catch (SQLException exception)
        {
            LOGGER.error("Could not update guild data!", exception);
        }
        return this;
    }

    public String getPrefix()
    {
        return dataObject.getString("command_prefix");
    }

    public TextChannel getLogChannel()
    {
        if (dataObject.isNull("log_channel")) return null;
        long id = dataObject.getLong("log_channel");
        return Bean.getInstance().getShardManager().getTextChannelById(id);
    }

    public Set<ReactionRole> getReactionRoles()
    {
        return reactionRoles;
    }

    @CheckReturnValue
    public GuildData addReactionRoles(ReactionRole... reactionRoles)
    {
        Checks.noneNull(reactionRoles, "Reaction roles");
        this.reactionRoles.addAll(Arrays.asList(reactionRoles));
        dataObject.put("reaction_roles", this.reactionRoles.stream().toList());
        return this;
    }

    @CheckReturnValue
    public GuildData removeReactionRoles(ReactionRole... reactionRoles)
    {
        Checks.noneNull(reactionRoles, "Reaction roles");
        Arrays.asList(reactionRoles).forEach(this.reactionRoles::remove);
        dataObject.put("reaction_roles", DataArray.fromCollection(this.reactionRoles));
        return this;
    }

    @CheckReturnValue
    public GuildData removeReactionRoles(long messageID)
    {
        reactionRoles = reactionRoles.stream()
                .filter(x -> x.getMessageId() != messageID)
                .collect(Collectors.toSet());
        dataObject.put("reaction_roles", DataArray.fromCollection(reactionRoles));
        return this;
    }

    public Set<Role> getModeratorRoles()
    {
        Set<Role> roles = new HashSet<>();
        if (dataObject.isNull("moderator_roles")) return roles;
        Long[] roleIds = dataObject.getArray("moderator_roles").stream(DataArray::getLong).toArray(Long[]::new);
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
        if (guild == null) return roles;
        for (long roleId : roleIds)
        {
            Role role = guild.getRoleById(roleId);
            if (role != null) roles.add(role);
        }
        return roles;
    }

    @CheckReturnValue
    public GuildData addModeratorRoles(Role... roles)
    {
        Checks.notEmpty(roles, "Roles");
        Set<Role> modRoles = getModeratorRoles();
        modRoles.addAll(Arrays.asList(roles));
        dataObject.put("moderator_roles", DataArray.fromCollection(modRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toList())));
        return this;
    }

    @CheckReturnValue
    public GuildData removeModeratorRoles(Role... roles)
    {
        Checks.notEmpty(roles, "Roles");
        Set<Role> modRoles = getModeratorRoles();
        Arrays.asList(roles).forEach(modRoles::remove);
        dataObject.put("moderator_roles", DataArray.fromCollection(modRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toList())));
        return this;
    }

    public boolean isModerator(Member member)
    {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        Set<Role> roles = getModeratorRoles();
        return CollectionUtils.containsAny(roles, member.getRoles());
    }

    public Set<Role> getDJRoles(boolean includeMods)
    {
        Set<Role> roles = new HashSet<>();
        if (includeMods) roles.addAll(getModeratorRoles());
        if (dataObject.isNull("dj_roles"))
            return roles;
        Long[] roleIds = dataObject.getArray("dj_roles").stream(DataArray::getLong).toArray(Long[]::new);
        Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
        if (guild == null) return roles;
        for (long roleId : roleIds)
        {
            Role role = guild.getRoleById(roleId);
            if (role != null) roles.add(role);
        }
        return roles;
    }

    @CheckReturnValue
    public GuildData addDJRoles(Role... roles)
    {
        Checks.notEmpty(roles, "Roles");
        Set<Role> djRoles = getDJRoles(false);
        djRoles.addAll(Arrays.asList(roles));
        dataObject.put("dj_roles", DataArray.fromCollection(djRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toList())));
        return this;
    }

    @CheckReturnValue
    public GuildData removeDJRoles(Role... roles)
    {
        Checks.notEmpty(roles, "Roles");
        Set<Role> djRoles = getDJRoles(false);
        Arrays.asList(roles).forEach(djRoles::remove);
        dataObject.put("dj_roles", DataArray.fromCollection(djRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toList())));
        return this;
    }

    public boolean isDJ(Member member)
    {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        Set<Role> roles = getDJRoles(true);
        if (CollectionUtils.containsAny(roles, member.getRoles())) return true;
        if (getDJMembers().contains(member.getIdLong())) return true;
        GuildVoiceState guildVoiceState = member.getVoiceState();
        if (guildVoiceState == null || guildVoiceState.getChannel() == null) return false;
        VoiceChannel channel = guildVoiceState.getChannel();
        return channel.getMembers().size() == 2 && channel.getMembers().contains(member.getGuild().getSelfMember());
    }

    public boolean isDJ(Role role)
    {
        return getDJRoles(false).contains(role);
    }

    public Set<Long> getDJMembers()
    {
        if (dataObject.isNull("dj_members"))
            return new HashSet<>();
        return dataObject.getArray("dj_members")
                        .stream(DataArray::getLong)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new));
    }

    @CheckReturnValue
    public GuildData addDJMembers(Member... members)
    {
        Checks.noneNull(members, "Members");
        Set<Long> djMembers = getDJMembers();
        djMembers.addAll(Arrays.stream(members).map(Member::getIdLong).collect(Collectors.toSet()));
        dataObject.put("dj_members", DataArray.fromCollection(djMembers.stream().toList()));
        return this;
    }

    @CheckReturnValue
    public GuildData removeDJMembers(Member... members)
    {
        Checks.notEmpty(members, "Members");
        Set<Long> djMembers = getDJMembers();
        Arrays.asList(members).stream().map(ISnowflake::getIdLong).forEach(djMembers::remove);
        dataObject.put("dj_members", DataArray.fromCollection(djMembers.stream().toList()));
        return this;
    }

    public Set<RoleReward> getRoleRewards()
    {
        if (dataObject.isNull("role_rewards"))
            return new HashSet<>();
        return dataObject.getArray("role_rewards")
                .stream(DataArray::getObject)
                .map(RoleReward::fromData)
                .collect(Collectors.toSet());
    }

    @CheckReturnValue
    public GuildData addRoleReward(int level, long roleId, boolean persist, boolean removeOnNextReward)
    {
        RoleReward roleReward = new RoleReward(level, roleId, persist, removeOnNextReward);
        Set<RoleReward> currentRewards = new HashSet<>(getRoleRewards());
        if (hasRoleReward(level))
            currentRewards.remove(getRoleReward(level));
        currentRewards.add(roleReward);
        dataObject.put("role_rewards", DataArray.fromCollection(currentRewards));
        return this;
    }

    @CheckReturnValue
    public GuildData removeRoleReward(int level)
    {
        if (!hasRoleReward(level)) return this;
        Set<RoleReward> currentRewards = new HashSet<>(getRoleRewards());
        currentRewards.removeIf(reward -> reward.getLevel() == level);
        dataObject.put("role_rewards", DataArray.fromCollection(currentRewards));
        return this;
    }

    public RoleReward getRoleReward(int level)
    {
        return getRoleRewards().stream().filter(reward -> reward.getLevel() == level).findFirst().orElse(null);
    }

    public boolean hasRoleReward(int level)
    {
        return getRoleRewards().stream().anyMatch(reward -> reward.getLevel() == level);
    }

    public RoleReward getLastRoleReward(int starting)
    {
        if (starting < 1) return null;
        Set<RoleReward> rewards = getRoleRewards();
        AtomicInteger integer = new AtomicInteger(starting);
        while (integer.get() > 0)
        {
            RoleReward reward = rewards.stream().filter(reward1 -> reward1.getLevel() == integer.get()).findFirst().orElse(null);
            if (reward != null)
                return reward;
            integer.decrementAndGet();
        }
        return null;
    }

    public Set<RoleReward> getAllRoleRewardsUpTo(int level)
    {
        if (level < 1) return Collections.emptySet();
        return getRoleRewards().stream().filter(reward -> reward.getLevel() <= level).collect(Collectors.toSet());
    }

    /**
     * Returns a {@link Set Set} of {@link RoleReward RoleRewards} that should be applied to a member with a given level.
     * This is not the same as {@link #getAllRoleRewardsUpTo(int)}!
     *
     * @param level The level
     * @return A Set of RoleRewards
     */
    public Set<RoleReward> getEffectiveRoleRewards(int level)
    {
        List<RoleReward> rewardList = new ArrayList<>(getAllRoleRewardsUpTo(level));
        if (rewardList.isEmpty())
            return Collections.emptySet();
        rewardList.sort(Comparator.comparingInt(RoleReward::getLevel));
        Set<RoleReward> result = new HashSet<>();
        RoleReward previous = null;
        for (RoleReward reward : rewardList)
        {
            if (previous != null && previous.doesRemoveOnNextReward())
            {
                result.remove(previous);
            }
            result.add(reward);
            previous = reward;
        }
        return result;
    }

    public DataObject toData()
    {
        return dataObject;
    }
}
