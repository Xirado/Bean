package at.xirado.bean.database.entity

import at.xirado.bean.data.cache.GuildCacheView
import at.xirado.bean.data.cache.UserCacheView
import at.xirado.bean.database.exposed.provideUsing
import at.xirado.bean.database.table.Members
import at.xirado.bean.leveling.strategy.Mee6LevelingStrategy
import at.xirado.bean.model.MemberExperienceStats
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Member(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<Member>(Members), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
        private val userCacheView by inject<UserCacheView>()
    }

    var user by Members.user provideUsing userCacheView
    var guild by Members.guild provideUsing guildCacheView
    var experience by Members.experience

    suspend fun getExperienceStats(withRank: Boolean = false): MemberExperienceStats {
        val strategy = guild.levelingConfig?.getStrategy() ?: Mee6LevelingStrategy

        val level = strategy.getLevel(experience)

        val rank = withRank.ifTrue { getRank() }

        val relativeXpToLevelUp = strategy.getExperienceToLevelUp(level)
        val relativeXp = experience - strategy.getExperienceNeededToReachLevel(level)

        return MemberExperienceStats(
            experience = experience,
            currentLevel = level,
            relativeXpToLevelUp = relativeXpToLevelUp,
            relativeXp = relativeXp,
            rank = rank,
        )
    }

    suspend fun getRank(): Int = newSuspendedTransaction {
        val sql = """
            SELECT rank
            FROM (
              SELECT "user", RANK() OVER (ORDER BY experience desc) as rank
              FROM members
              WHERE guild = ?
            ) AS ranked_members
            WHERE ranked_members."user" = ?
        """.trimIndent()

        val longColumnType = LongColumnType()
        val args = listOf(
            longColumnType to guild.id.value,
            longColumnType to user.id.value,
        )

        exec(sql, args, StatementType.SELECT) {
            if (it.next())
                it.getInt("rank")
            else
                -1
        } ?: -1
    }

    override fun toString(): String {
        return "Member(user=$user, guild=$guild)"
    }
}