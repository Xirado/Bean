package at.xirado.bean.leveling

import at.xirado.bean.Config
import at.xirado.bean.data.DBContext
import at.xirado.bean.data.cache.MemberCacheView
import at.xirado.bean.database.entity.GuildLevelingConfig
import at.xirado.bean.database.entity.Member
import at.xirado.bean.database.table.NotificationType
import at.xirado.bean.jda.JDAEventListener
import at.xirado.bean.model.GuildFeature
import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.seconds

private val xpTimeout = 1.seconds.inWholeMilliseconds
private val log = KotlinLogging.logger { }

@Single
class LevelingService(
    private val memberCacheView: MemberCacheView,
    private val db: DBContext,
    private val config: Config,
) : JDAEventListener, KoinComponent {
    private val defaultFeatures = config.defaultGuildFeaturesParsed
    private val timeouts: MutableMap<Long, Long> = mutableMapOf()
    private val timeoutLock = ReentrantLock()

    init {
        setupTimeoutRemovalTask()
    }

    override suspend fun onEvent(event: GenericEvent) {
        if (event !is MessageReceivedEvent) return

        if (!event.isFromGuild) return

        processLeveling(event)
    }

    suspend fun processLeveling(event: MessageReceivedEvent) {
        if (!event.isFromGuild || event.isWebhookMessage || event.author.isBot)
            return

        val user = event.author
        val userId = user.idLong

        db.doTransaction {
            val jdaMember = event.member!!
            val member = memberCacheView.save(jdaMember)

            val guild = member.guild
            val guildFeatures = guild.features ?: defaultFeatures

            if (GuildFeature.LEVELING !in guildFeatures)
                return@doTransaction

            timeoutLock.withLock {
                val timeout = timeouts[userId]
                val now = System.currentTimeMillis()

                if (timeout != null && timeout + xpTimeout > now)
                    return@doTransaction
            }

            val levelingConfig = guild.levelingConfig ?: run {
                val default = createDefaultLevelingConfig(guild.id.value)
                guild.levelingConfig = default
                default
            }

            val strategy = levelingConfig.getStrategy()

            val baseXpGained = Random.nextInt(15..25)
            val xpWithMultiplier = (baseXpGained * levelingConfig.multiplier).toInt()

            val currentLevel = strategy.getLevel(member.experience)
            val totalXpToReachNextLevel = strategy.getExperienceNeededToReachLevel(currentLevel + 1)
            val xpToLevelUp = totalXpToReachNextLevel - member.experience

            member.experience += xpWithMultiplier
            if (xpToLevelUp < xpWithMultiplier) {
                // Recalculating this in case the member ranks up more than 1 level (relevant for custom strategies)
                val newLevel = strategy.getLevel(member.experience)
                try {
                    handleLevelUp(event, member, levelingConfig, newLevel)
                } catch (e: Exception) {
                    log.error(e) { "Failed to handle level up for member $member" }
                }
            }

            timeoutLock.withLock {
                timeouts[userId] = System.currentTimeMillis()
            }
        }
    }

    private suspend fun handleLevelUp(
        event: MessageReceivedEvent,
        member: Member,
        levelingConfig: GuildLevelingConfig,
        newLevel: Int
    ) {
        val jdaMember = event.member!!
        event.channel.sendMessage("Congrats ${jdaMember.asMention}, you reached level $newLevel").await()
    }

    private fun createDefaultLevelingConfig(guildId: Long) = GuildLevelingConfig.new(guildId) {
        notificationType = NotificationType.CURRENT_CHANNEL
    }

    private fun setupTimeoutRemovalTask() {
        Thread.ofVirtual().start {
            while (true) {
                try {
                    timeoutLock.withLock {
                        val now = System.currentTimeMillis()
                        val iterator = timeouts.iterator()

                        while (iterator.hasNext()) {
                            val entry = iterator.next()
                            if (entry.value + xpTimeout < now)
                                iterator.remove()
                        }
                    }
                } catch (e: Exception) {
                    if (e is InterruptedException) {
                        log.warn(e) { "XP timeout invalidator thread interrupted" }
                        Thread.currentThread().interrupt()
                        break
                    }
                    log.error(e) { "Failed to invalidate xp timeouts" }
                }

                Thread.sleep(60000)
            }
        }
    }
}