package at.xirado.bean.data;


import at.xirado.bean.Bean;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.RoleReward;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.sql.Connection;
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
        ReactionRole[] reactions = json.convertValueAt("reaction_roles", ReactionRole[].class);
        if (reactions != null && reactions.length > 0)
            reactionRoles.addAll(Arrays.asList(reactions));
    }

    public <T> T convertValueAt(String path, Class<T> type)
    {
        return dataObject.convertValueAt(path, type);
    }

    public GuildData put(String key, Object object)
    {
        dataObject.put(key, object);
        return this;
    }

    public String toJson() throws JsonProcessingException
    {
        return dataObject.toJson();
    }

    public String toPrettyString() throws JsonProcessingException
    {
        return dataObject.toPrettyString();
    }

    public GuildData putNull(String key)
    {
        dataObject.putNull(key);
        return this;
    }

    public GuildData setRoot(String root)
    {
        dataObject.setRoot(root);
        return this;
    }

    public String getRoot(String root)
    {
        return dataObject.getRoot();
    }

    public String getString(String query, Object... objects)
    {
        return dataObject.getString(query, objects);
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

    public Float getFloat(String query)
    {
        return dataObject.getFloat(query);
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
        return dataObject.get(query, type);
    }

    public String[] getMetaData()
    {
        return dataObject.getMetadata();
    }

    public GuildData setMetaData(String[] metaData)
    {
        dataObject.setMetadata(metaData);
        return this;
    }

    // guild specific getters and setters

    public GuildData update()
    {
        Connection connection = Database.getConnectionFromPool();
        String query = "INSERT INTO guildSettings (guildID, data) values (?,?) ON DUPLICATE KEY UPDATE data = ?";
        try (var ps = connection.prepareStatement(query))
        {
            String jsonString = dataObject.toJson();
            ps.setLong(1, guildID);
            ps.setString(2, jsonString);
            ps.setString(3, jsonString);
            ps.execute();
        } catch (SQLException | JsonProcessingException exception)
        {
            LOGGER.error("Could not update guild data!", exception);
        } finally
        {
            Util.closeQuietly(connection);
        }
        return this;
    }

    public String getPrefix()
    {
        return dataObject.getString("command_prefix");
    }

    public TextChannel getLogChannel()
    {
        Long id = dataObject.getLong("log_channel");
        if (id == null) return null;
        return Bean.getInstance().getJDA().getTextChannelById(id);
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
        dataObject.put("reaction_roles", this.reactionRoles);
        return this;
    }

    @CheckReturnValue
    public GuildData removeReactionRoles(ReactionRole... reactionRoles)
    {
        Checks.noneNull(reactionRoles, "Reaction roles");
        Arrays.asList(reactionRoles).forEach(this.reactionRoles::remove);
        dataObject.put("reaction_roles", this.reactionRoles);
        return this;
    }

    @CheckReturnValue
    public GuildData removeReactionRoles(long messageID)
    {
        reactionRoles = reactionRoles.stream()
                .filter(x -> x.getMessageId() != messageID)
                .collect(Collectors.toSet());
        dataObject.put("reaction_roles", reactionRoles);
        return this;
    }

    public Set<Role> getModeratorRoles()
    {
        Set<Role> roles = new HashSet<>();
        if (dataObject.getObject("moderator_roles") == null) return roles;
        long[] roleIds = dataObject.convertValueAt("moderator_roles", long[].class);
        Guild guild = Bean.getInstance().getJDA().getGuildById(guildID);
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
        dataObject.put("moderator_roles", modRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toSet()));
        return this;
    }

    @CheckReturnValue
    public GuildData removeModeratorRoles(Role... roles)
    {
        Checks.notEmpty(roles, "Roles");
        Set<Role> modRoles = getModeratorRoles();
        Arrays.asList(roles).forEach(modRoles::remove);
        dataObject.put("moderator_roles", modRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toSet()));
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
        if (dataObject.getObject("dj_roles") == null)
            return roles;
        long[] roleIds = dataObject.convertValueAt("dj_roles", long[].class);
        Guild guild = Bean.getInstance().getJDA().getGuildById(guildID);
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
        dataObject.put("dj_roles", djRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toSet()));
        return this;
    }

    @CheckReturnValue
    public GuildData removeDJRoles(Role... roles)
    {
        Checks.notEmpty(roles, "Roles");
        Set<Role> djRoles = getDJRoles(false);
        Arrays.asList(roles).forEach(djRoles::remove);
        dataObject.put("dj_roles", djRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toSet()));
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
        Set<Long> members = new HashSet<>();
        if (dataObject.getObject("dj_members") == null)
            return members;
        Long[] memberIds = dataObject.convertValueAt("dj_members", Long[].class);
        Arrays.stream(memberIds).filter(Objects::nonNull).forEach(members::add);
        return members;
    }

    @CheckReturnValue
    public GuildData addDJMembers(Member... members)
    {
        Checks.notEmpty(members, "Members");
        Set<Long> djMembers = getDJMembers();
        djMembers.addAll(Arrays.stream(members).map(Member::getIdLong).collect(Collectors.toSet()));
        dataObject.put("dj_members", djMembers);
        return this;
    }

    @CheckReturnValue
    public GuildData removeDJMembers(Member... members)
    {
        Checks.notEmpty(members, "Members");
        Set<Long> djMembers = getDJMembers();
        Arrays.asList(members).stream().map(ISnowflake::getIdLong).forEach(djMembers::remove);
        dataObject.put("dj_members", djMembers);
        return this;
    }

    public Set<RoleReward> getRoleRewards()
    {
        if (dataObject.getObject("role_rewards") == null)
            return Collections.emptySet();
        return Set.of(dataObject.convertValueAt("role_rewards", RoleReward[].class));
    }

    @CheckReturnValue
    public GuildData addRoleReward(int level, long roleId, boolean persist, boolean removeOnNextReward)
    {
        RoleReward roleReward = new RoleReward();
        roleReward.setLevel(level);
        roleReward.setRoleId(roleId);
        roleReward.setPersist(persist);
        roleReward.setRemoveOnNextReward(removeOnNextReward);
        Set<RoleReward> currentRewards = new HashSet<>(getRoleRewards());
        if (hasRoleReward(level))
            currentRewards.remove(getRoleReward(level));
        currentRewards.add(roleReward);
        dataObject.put("role_rewards", currentRewards);
        return this;
    }

    @CheckReturnValue
    public GuildData removeRoleReward(int level)
    {
        if (!hasRoleReward(level)) return this;
        Set<RoleReward> currentRewards = new HashSet<>(getRoleRewards());
        currentRewards.removeIf(reward -> reward.getLevel() == level);
        dataObject.put("role_rewards", currentRewards);
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
     * Returns a {@link java.util.Set Set} of {@link RoleReward RoleRewards} that should be applied to a member with a given level.
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

}
