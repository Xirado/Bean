package at.xirado.bean.data.database.entity

import at.xirado.bean.data.ReactionRole
import at.xirado.bean.data.RoleReward
import at.xirado.bean.data.database.table.DiscordGuilds
import net.dv8tion.jda.api.entities.Member
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordGuild(id: EntityID<Long>) : Entity<Long>(id) {
    var moderatorRoles by DiscordGuilds.moderatorRoles
    var logChannel by DiscordGuilds.logChannel
    var reactionRoles by DiscordGuilds.reactionRoles
    var roleRewards by DiscordGuilds.roleRewards
    var silentLevelingChannels by DiscordGuilds.silentLevelingChannels

    fun isModerator(member: Member) = member.roles.any { it.idLong in moderatorRoles }

    fun getRoleReward(level: Int) = roleRewards.firstOrNull { it.level == level }

    fun hasRoleReward(level: Int) = getRoleReward(level) != null

    fun addRoleReward(level: Int, roleId: Long, persists: Boolean, removeOnNextReward: Boolean) {
        addRoleReward(RoleReward(level, roleId, persists, removeOnNextReward))
    }

    fun addRoleReward(roleReward: RoleReward) {
        transaction(db) {
            val filteredRoleRewards = roleRewards.filterNot { it.level == roleReward.level }
            roleRewards = (filteredRoleRewards + roleReward).toSet()
        }
    }

    fun removeRoleReward(level: Int) {
        transaction(db) {
            val toRemove = getRoleReward(level) ?: return@transaction
            roleRewards -= toRemove
        }
    }

    fun getLastRoleReward(upto: Int): RoleReward? {
        if (upto < 1) return null
        return roleRewards.filter { it.level <= upto }.maxByOrNull { it.level }
    }

    fun getEffectiveRoleRewards(level: Int): Set<RoleReward> {
        val allRewards = roleRewards.filter { it.level <= level }.sortedBy { it.level }
        if (allRewards.isEmpty())
            return emptySet()

        val effectiveRewards = mutableListOf<RoleReward>()
        var previousReward: RoleReward? = null

        for (reward in allRewards) {
            if (previousReward?.removeOnNextReward == true)
                effectiveRewards -= previousReward

            effectiveRewards += reward
            previousReward = reward
        }

        return effectiveRewards.toSet()
    }

    fun addModeratorRole(roleId: Long) {
        transaction(db) {
            if (roleId !in moderatorRoles)
                moderatorRoles += roleId
        }
    }

    fun removeModeratorRole(roleId: Long) {
        transaction(db) {
            if (roleId in moderatorRoles)
                moderatorRoles -= roleId
        }
    }

    fun getReactionRoles(messageId: Long): Set<ReactionRole> = reactionRoles.filter {
        it.messageId == messageId
    }.toSet()

    fun getReactionRole(messageId: Long, emoji: String) = reactionRoles.find { it.messageId == messageId && it.emote == emoji }

    fun addReactionRole(reactionRole: ReactionRole) {
        transaction(db) {
            if (reactionRoles.any { it.messageId == reactionRole.messageId && it.emote == reactionRole.emote })
                throw IllegalStateException("Duplicate reaction role $reactionRole")

            reactionRoles += reactionRole
        }
    }

    fun removeReactionRole(reactionRole: ReactionRole) {
        transaction(db) {
            if (reactionRole !in reactionRoles)
                throw IllegalStateException("No such reaction role $reactionRole")

            reactionRoles -= reactionRole
        }
    }

    fun removeReactionRoles(messageId: Long) {
        transaction(db) {
            reactionRoles = reactionRoles.filterNot { it.messageId == messageId }.toSet()
        }
    }

    companion object : EntityClass<Long, DiscordGuild>(DiscordGuilds)
}